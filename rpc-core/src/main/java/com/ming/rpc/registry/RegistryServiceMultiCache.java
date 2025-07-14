package com.ming.rpc.registry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ming.rpc.model.ServiceMetaInfo;

/**
 * 注册中心服务本地缓存（支持多个服务键名缓存）
 */
public class RegistryServiceMultiCache {
    /**
     * 服务缓存
     */
     Map<String, List<ServiceMetaInfo>> serviceCache = new  ConcurrentHashMap<>();

    /**
     * 写缓存
     * @param serviceKey 服务键名
     * @param newServiceCache 新服务缓存
     * @return
     */
    void writeCache(String serviceKey, List<ServiceMetaInfo> newServiceCache) {
        this.serviceCache.put(serviceKey, newServiceCache);
    }

    /**
     * 读缓存
     * @param serviceKey 服务键名
     * @return
     */
    List<ServiceMetaInfo> readCache(String serviceKey) {
        return this.serviceCache.get(serviceKey);
    }

    /**
     * 清空缓存
     */
    void clearCache(String serviceKey) {
        this.serviceCache.remove(serviceKey);
    }
}
