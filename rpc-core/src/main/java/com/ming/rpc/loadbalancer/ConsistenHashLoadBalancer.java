package com.ming.rpc.loadbalancer;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ming.rpc.model.ServiceMetaInfo;
import com.ming.rpc.utils.MurmurHashUtil;

/**
 * 一致性哈希负载均衡器
 */
public class ConsistenHashLoadBalancer implements LoadBalancer {

    /**
     * 使用TreeMap定义一致性哈希环，存放虚拟节点
     */
    private final TreeMap<Integer, ServiceMetaInfo> virtualNodes = new TreeMap<>();
   
    /**
     * 虚拟节点数
     */
    private static final int VIRTUAL_NODES = 100;
   
   
    /**
     * 选择服务实例
     * @param requestParams 请求参数
     * @param serviceMetaInfoList 可用服务实例列表
     * @return 选择的服务实例
     */
    @Override
    public ServiceMetaInfo select(Map<String , Object> requestParams,List<ServiceMetaInfo> serviceMetaInfoList) {
       if(serviceMetaInfoList == null || serviceMetaInfoList.isEmpty()){
        return null;
       }
       // 将服务实例列表转换为虚拟节点列表
       for(ServiceMetaInfo serviceMetaInfo : serviceMetaInfoList){
        for(int i = 0; i < VIRTUAL_NODES; i++){
            // 计算虚拟节点的哈希值
            int hash = getHash(serviceMetaInfo.getServiceKey() + "#" + i);
            // 将虚拟节点添加到一致性哈希环中
            virtualNodes.put(hash, serviceMetaInfo);
        }
       }


       //获取调用请求的hash 值
       int hash = getHash(requestParams);

       //选择最接近且大于等于调用请求hash值的虚拟节点
       Map.Entry<Integer, ServiceMetaInfo> entry = virtualNodes.ceilingEntry(hash);
       if(entry == null){
        //如果没有大于恒宇调用请求hash值的虚拟节点，则返回环首部的节点
        entry = virtualNodes.firstEntry();
       }
       //返回虚拟节点对应的服务实例
       return entry.getValue();

    }

    /**
     * Hash 算法，改用 MurmurHash 以获得更好的分布性
     * 之前使用 key.hashCode() 的方法，其分布性不佳，容易导致数据倾斜，
     * 某些节点负载过高。MurmurHash 是一种高性能、低碰撞的哈希算法，
     * 能更均匀地将 key 映射到哈希环上，从而实现更均衡的负载分配。
     * @param key 
     * @return 
     */
    private int getHash(Object key){
        return MurmurHashUtil.hash32(key.toString().getBytes());
    }

    
}
