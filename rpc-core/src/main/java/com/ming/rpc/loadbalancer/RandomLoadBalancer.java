package com.ming.rpc.loadbalancer;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ming.rpc.model.ServiceMetaInfo;

/**
 * 随机负载均衡器
 */
public class RandomLoadBalancer implements LoadBalancer {
    private final Random random = new Random();
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
        //只有一个服务，不用随机
        int size = serviceMetaInfoList.size();
        if(size == 1) {
            return serviceMetaInfoList.get(0);
        }
        return serviceMetaInfoList.get(random.nextInt(size));
    }

}
