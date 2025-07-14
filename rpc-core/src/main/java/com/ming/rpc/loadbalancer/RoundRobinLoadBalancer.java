package com.ming.rpc.loadbalancer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.ming.rpc.model.ServiceMetaInfo;

/**
 * 轮询负载均衡器
 */
public class RoundRobinLoadBalancer implements LoadBalancer {
    /**
     * 当前轮询的下标
     */
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    /**
     * 选择服务实例
     * @param requestParams 请求参数
     * @param serviceMetaInfoList 可用服务实例列表
     * @return 选择的服务实例
     */
    @Override
    public ServiceMetaInfo select(Map<String , Object> requestParams,List<ServiceMetaInfo> serviceMetaInfoList) {
        if(serviceMetaInfoList == null || serviceMetaInfoList.isEmpty()) {
        return null;
        }
        //只有一个福娃五，无需轮询
        int size = serviceMetaInfoList.size();
        if(size == 1) {
            return serviceMetaInfoList.get(0);
        }
        // 取模算法轮询，防止下标越界
        int index = currentIndex.getAndIncrement() % size;
        return serviceMetaInfoList.get(index);
    }
}
