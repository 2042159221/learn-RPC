package com.ming.rpc.registry;

import java.util.List;

import com.ming.rpc.model.ServiceMetaInfo;



/**
 * 注册中心本地缓存（ 用于维护续期）
 */
public class RegistryServiceCache {
    /**
     * 服务缓存
     */
    List<ServiceMetaInfo> serviceCache ;

    /**
     * 写缓存
     */
    void writeCache(List<ServiceMetaInfo> newServiceCache) {
        this.serviceCache = newServiceCache;
    }

    /**
     * 读缓存
     */
    List<ServiceMetaInfo> readCache() { 
        return this.serviceCache;
    }

    /**
     * 清空缓存
     */
    void clearCache() {
        this.serviceCache.clear();
    }

}
