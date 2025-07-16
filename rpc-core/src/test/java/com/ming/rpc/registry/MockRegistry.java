package com.ming.rpc.registry;

import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于测试的注册中心实现
 */
public class MockRegistry implements Registry {

    /**
     * 注册信息存储
     */
    private final Map<String, List<ServiceMetaInfo>> registryMap = new ConcurrentHashMap<>();

    @Override
    public void init(RegistryConfig registryConfig) {
        // 测试无需初始化
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        List<ServiceMetaInfo> serviceMetaInfos = registryMap.getOrDefault(serviceMetaInfo.getServiceKey(), new ArrayList<>());
        serviceMetaInfos.add(serviceMetaInfo);
        registryMap.put(serviceMetaInfo.getServiceKey(), serviceMetaInfos);
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        List<ServiceMetaInfo> serviceMetaInfos = registryMap.getOrDefault(serviceMetaInfo.getServiceKey(), new ArrayList<>());
        serviceMetaInfos.remove(serviceMetaInfo);
        registryMap.put(serviceMetaInfo.getServiceKey(), serviceMetaInfos);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        return registryMap.getOrDefault(serviceKey, new ArrayList<>());
    }

    @Override
    public void destroy() {
        registryMap.clear();
    }

    @Override
    public void heartbeat() {
        // 测试无需心跳
    }

    @Override
    public void watch(String serviceKey) {
        // 测试无需监听
    }
} 