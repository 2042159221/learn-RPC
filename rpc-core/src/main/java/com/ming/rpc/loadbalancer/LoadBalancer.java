package com.ming.rpc.loadbalancer;

import java.util.List;
import java.util.Map;

import com.ming.rpc.model.ServiceMetaInfo;

/**
 * 负载均衡器（消费端使用）
 */
public interface LoadBalancer {
    /**
     * 选择服务实例
     * @param requestParams 请求参数
     * @param serviceMetaInfoList 可用服务实例列表
     * @return 选择的服务实例
     */
    ServiceMetaInfo select(Map<String , Object> requestParams,List<ServiceMetaInfo> serviceMetaInfoList);
}
