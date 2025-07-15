package com.ming.rpc.registry;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;

import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;

import cn.hutool.core.collection.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;

/**
 * ZooKeeper 注册中心
 * 操作文档：<a href="https://curator.apache.org/docs/getting-started">Apache Curator</a>
 * 代码示例：<a href="https://github.com/apache/curator/blob/master/curator-examples/src/main/java/discovery/DiscoveryExample.java">DiscoveryExample.java</a>
 * 监听 key 示例：<a href="https://github.com/apache/curator/blob/master/curator-examples/src/main/java/cache/CuratorCacheExample.java">CuratorCacheExample.java</a>
 *
 */
@Slf4j
public class ZooKeeperRgistry implements Registry {
    private CuratorFramework client;
    private ServiceDiscovery<ServiceMetaInfo> serviceDiscovery;

    /**
     * 本机注册节点key的集合（用于维护续期）
     * 
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 注册中心服务缓存
     * 
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * 正在监听的 key集合
     * 
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * 根节点
     */
    private static final String ZK_ROOT_PATH = "/rpc/zk";

    @Override
    public void init(RegistryConfig registryConfig) {
        // 构建client 实例
        client = CuratorFrameworkFactory.builder()
            .connectString(registryConfig.getAddress())
            .retryPolicy(new ExponentialBackoffRetry(Math.toIntExact(registryConfig.getTimeout()), 3))
            .build();

        // 构建服务发现实例
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetaInfo.class)
            .client(client)
            .basePath(ZK_ROOT_PATH)
            .serializer(new JsonInstanceSerializer<>(ServiceMetaInfo.class))
            .build();

        try{
            //启动client 和 serviceDiscovery
            client.start();
            serviceDiscovery.start();
        }catch(Exception e){
            log.error("ZooKeeperRegistry init error",e);
            throw new RuntimeException("ZooKeeperRegistry init error",e);
        }
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 注册服务
        serviceDiscovery.registerService(buildServiceInstance(serviceMetaInfo));
        // 添加节点信息到本地缓存
        String registerKey = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        localRegisterNodeKeySet.add(registerKey);
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        try{
            serviceDiscovery.unregisterService(buildServiceInstance(serviceMetaInfo));
        }catch(Exception e){
            log.error("unregister service error",e);
            throw new RuntimeException("unregister service error",e);
        }
        // 从本地缓存中移除节点信息
        String registerKey = ZK_ROOT_PATH + "/" + serviceMetaInfo.getServiceNodeKey();
        localRegisterNodeKeySet.remove(registerKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        //优先从缓存获取服务
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        if(cachedServiceMetaInfoList != null && !cachedServiceMetaInfoList.isEmpty()){
            return cachedServiceMetaInfoList;
        }
        try{
            //查询服务信息
            Collection<ServiceInstance<ServiceMetaInfo>> serviceInstances = serviceDiscovery.queryForInstances(serviceKey);
            //解析服务信息
            List<ServiceMetaInfo> serviceMetaInfoList = serviceInstances.stream()
                .map(ServiceInstance::getPayload)
                .collect(Collectors.toList());
            
            //写入服务缓存
            registryServiceCache.writeCache(serviceMetaInfoList);
            return serviceMetaInfoList;
        }catch(Exception e){
            log.error("serviceDiscovery error",e);
            throw new RuntimeException("serviceDiscovery error",e);
        }
    }

    @Override
    public void heartbeat() {
        //不需要心跳机制，建立了临时节点，如果服务器故障，则临时节点直接丢失
    }


    /**
     * 监听（消费端）
     * @param serviceNodeKey 服务节点key
     */
    @Override
    public void watch(String serviceNodeKey) {
        String watchKey = ZK_ROOT_PATH + "/" + serviceNodeKey;
        boolean newWatch = watchingKeySet.add(watchKey);
        if(newWatch){
               CuratorCache curatorCache = CuratorCache.build(client, watchKey);
               curatorCache.start();
               curatorCache.listenable().addListener(
                    CuratorCacheListener.builder()
                        .forAll((type, oldData, data) -> registryServiceCache.clearCache())
                        .build()
               );
        }
    }


    @Override
    public void destroy() {
        log.info("ZooKeeperRegistry destroy");
        //下线节点（这一步可以不做，因为是临时节点，服务下线，自然就被删掉了
        for(String localRegisterNodeKey : localRegisterNodeKeySet){
            try{
                client.delete().forPath(localRegisterNodeKey);
            }catch(Exception e){
                log.error("unregister service error",e);
                throw new RuntimeException("unregister service error",e);
            }
        }
        //释放资源
        if (client != null){
            client.close();
        }
    }
    private ServiceInstance<ServiceMetaInfo> buildServiceInstance(ServiceMetaInfo serviceMetaInfo){
        String serviceAddress = serviceMetaInfo.getServiceHost() + ":" + serviceMetaInfo.getServicePort();
        try{
            return ServiceInstance
                .<ServiceMetaInfo>builder()
                .id(serviceAddress)
                .name(serviceMetaInfo.getServiceKey())
                .address(serviceAddress)
                .payload(serviceMetaInfo)
                .build();
        }catch(Exception e){
            log.error("buildServiceInstance error",e);
            throw new RuntimeException("buildServiceInstance error",e);
        }
    }
}
