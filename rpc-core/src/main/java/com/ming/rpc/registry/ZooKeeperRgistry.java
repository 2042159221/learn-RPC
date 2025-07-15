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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;

import cn.hutool.core.collection.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;

/**
 * ZooKeeper 注册中心
 */
@Slf4j
public class ZooKeeperRgistry implements Registry {
    private CuratorFramework client;
    private ServiceDiscovery<ServiceMetaInfo> serviceDiscovery;

    /**
     * 本机注册的节点 key 集合（用于维护续期）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 注册中心服务缓存
     */
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    /**
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();

    /**
     * 正在监听的缓存集合
     */
    private final Map<String, CuratorCache> watchingCacheMap = new ConcurrentHashMap<>();


    private static final String ZK_ROOT_PATH = "/rpc/zk";

    @Override
    public void init(RegistryConfig registryConfig) {
        // 构建 client 实例
        client = CuratorFrameworkFactory
                .builder()
                .connectString(registryConfig.getAddress())
                .retryPolicy(new ExponentialBackoffRetry(Math.toIntExact(registryConfig.getTimeout()), 3))
                .build();

        // 构建 serviceDiscovery 实例
        serviceDiscovery = ServiceDiscoveryBuilder.builder(ServiceMetaInfo.class)
                .client(client)
                .basePath(ZK_ROOT_PATH)
                .serializer(new JsonInstanceSerializer<>(ServiceMetaInfo.class))
                .build();

        try {
            // 启动 client 和 serviceDiscovery
            client.start();
            serviceDiscovery.start();
        } catch (Exception e) {
            throw new RuntimeException("启动 ZooKeeper 客户端失败", e);
        }
    }


    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        // 注册到 ZK 中
        serviceDiscovery.registerService(buildServiceInstance(serviceMetaInfo));

        // 添加节点信息到本地缓存
        String serviceNodeKey = buildServiceNodeKey(serviceMetaInfo);
        localRegisterNodeKeySet.add(serviceNodeKey);
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        try {
            serviceDiscovery.unregisterService(buildServiceInstance(serviceMetaInfo));
        } catch (Exception e) {
            throw new RuntimeException("注销服务失败", e);
        }
        // 从本地缓存移除
        String serviceNodeKey = buildServiceNodeKey(serviceMetaInfo);
        localRegisterNodeKeySet.remove(serviceNodeKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // 优先从缓存获取
        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache(serviceKey);
        if (cachedServiceMetaInfoList != null) {
            return cachedServiceMetaInfoList;
        }
        // 缓存未命中，从注册中心读取并设置监听
        return discoverAndCache(serviceKey);
    }


    @Override
    public void destroy() {
        log.info("开始销毁ZooKeeper注册中心...");
        // 依次关闭所有watch缓存
        for (CuratorCache curatorCache : watchingCacheMap.values()) {
            try {
                curatorCache.close();
            } catch (Exception e) {
                log.error("关闭 CuratorCache 失败", e);
            }
        }
        // 关闭 serviceDiscovery
        if (serviceDiscovery != null) {
            try {
                serviceDiscovery.close();
            } catch (IOException e) {
                log.error("关闭 ServiceDiscovery 失败", e);
            }
        }
        // 关闭 client
        if (client != null) {
            client.close();
        }
    }

    @Override
    public void heartbeat() {
        // 不需要心跳，由 Curator 的临时节点机制保障
    }

    @Override
    public void watch(String serviceKey) {
        String serviceNodePath = ZK_ROOT_PATH + "/" + serviceKey;
        if (watchingKeySet.contains(serviceNodePath)) {
            return;
        }
        watchingKeySet.add(serviceNodePath);
        CuratorCache curatorCache = CuratorCache.build(client, serviceNodePath);

        curatorCache.listenable().addListener((type, oldData, data) -> {
            log.info("ZooKeeper watch event: type={}, path={}", type, oldData != null ? oldData.getPath() : "null");
            // 监听到变化后，重新发现并缓存服务
            discoverAndCache(serviceKey);
        });

        try {
            curatorCache.start();
            watchingCacheMap.put(serviceNodePath, curatorCache);
        } catch (Exception e) {
            throw new RuntimeException("启动 ZooKeeper watch 失败", e);
        }
    }

    private String buildServiceNodeKey(ServiceMetaInfo serviceMetaInfo) {
        return String.join("/", serviceMetaInfo.getServiceKey(), serviceMetaInfo.getServiceAddress());
    }


    private ServiceInstance<ServiceMetaInfo> buildServiceInstance(ServiceMetaInfo serviceMetaInfo) {
        String serviceAddress = serviceMetaInfo.getServiceAddress();
        try {
            return ServiceInstance.<ServiceMetaInfo>builder()
                    .id(String.format("%s:%d", serviceMetaInfo.getServiceHost(), serviceMetaInfo.getServicePort())) // 使用 host:port 作为唯一ID
                    .name(serviceMetaInfo.getServiceKey())
                    .address(serviceAddress)
                    .payload(serviceMetaInfo)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("构建服务实例失败", e);
        }
    }

    /**
     * 发现并缓存服务
     *
     * @param serviceKey 服务键
     * @return 服务列表
     */
    private List<ServiceMetaInfo> discoverAndCache(String serviceKey) {
        try {
            // 从注册中心读取
            Collection<ServiceInstance<ServiceMetaInfo>> serviceInstanceCollection = serviceDiscovery.queryForInstances(serviceKey);
            // 首次发现，建立监听
            watch(serviceKey);
            // 写入缓存
            List<ServiceMetaInfo> serviceMetaInfoList = serviceInstanceCollection.stream()
                    .map(ServiceInstance::getPayload)
                    .collect(Collectors.toList());
            registryServiceCache.writeCache(serviceKey, serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            // 检查异常链中是否包含 MismatchedInputException
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException) {
                    // 当服务下线导致节点内容为空时，会抛出此异常，属于正常情况
                    log.info("Service '{}' has no active instances, which is considered a normal scenario.", serviceKey);
                    // 写入空列表到缓存
                    registryServiceCache.writeCache(serviceKey, Collections.emptyList());
                    return Collections.emptyList();
                }
                cause = cause.getCause();
            }
            throw new RuntimeException("从注册中心刷新服务失败", e);
        }
    }
}
