package com.ming.rpc.registry;

import com.ming.rpc.model.ServiceMetaInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 注册中心服务本地缓存
 */
public class RegistryServiceCache {

    /**
     * 服务缓存
     */
    private final Map<String, List<ServiceMetaInfo>> serviceCache = new ConcurrentHashMap<>();

    /**
     * 写入缓存
     *
     * @param serviceKey        服务键
     * @param newServiceMetaInfoList 新的服务列表
     */
    void writeCache(String serviceKey, List<ServiceMetaInfo> newServiceMetaInfoList) {
        this.serviceCache.put(serviceKey, newServiceMetaInfoList);
    }

    /**
     * 读缓存
     *
     * @param serviceKey 服务键
     * @return 服务列表
     */
    List<ServiceMetaInfo> readCache(String serviceKey) {
        return this.serviceCache.get(serviceKey);
    }

    /**
     * 清空缓存
     */
    void clearCache() {
        this.serviceCache.clear();
    }
}
