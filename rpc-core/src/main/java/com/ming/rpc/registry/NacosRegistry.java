package com.ming.rpc.registry;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;
import com.ming.rpc.serializer.Serializer;
import com.ming.rpc.serializer.SerializerFactory;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NacosRegistry implements Registry {

    private static final Logger LOGGER = LoggerFactory.getLogger(NacosRegistry.class);

    private NamingService namingService;
    private final Serializer serializer = SerializerFactory.getInstance("json");
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    @Override
    public void init(RegistryConfig registryConfig) {
        try {
            namingService = NamingFactory.createNamingService(registryConfig.getAddress());
        } catch (NacosException e) {
            LOGGER.error("Failed to initialize Nacos registry", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        try {
            Instance instance = new Instance();
            instance.setIp(serviceMetaInfo.getServiceHost());
            instance.setPort(serviceMetaInfo.getServicePort());
            instance.setClusterName("DEFAULT"); // Or from config
            Map<String, String> metadata = new HashMap<>();
            metadata.put("serviceMetaInfo", new String(serializer.serialize(serviceMetaInfo)));
            instance.setMetadata(metadata);

            namingService.registerInstance(serviceMetaInfo.getServiceKey(), instance);
        } catch (NacosException e) {
            LOGGER.error("Failed to register service to Nacos", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        try {
            namingService.deregisterInstance(serviceMetaInfo.getServiceKey(), serviceMetaInfo.getServiceHost(), serviceMetaInfo.getServicePort());
        } catch (NacosException e) {
            LOGGER.error("Failed to unregister service from Nacos", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        // First, try to read from local cache
        List<ServiceMetaInfo> cachedServices = registryServiceCache.readCache(serviceKey);
        if (cachedServices != null && !cachedServices.isEmpty()) {
            return cachedServices;
        }

        try {
            List<Instance> instances = namingService.selectInstances(serviceKey, true);
            List<ServiceMetaInfo> serviceMetaInfoList = instances.stream()
                    .map(instance -> {
                        try {
                            String serviceMetaInfoJson = instance.getMetadata().get("serviceMetaInfo");
                            return serializer.deserialize(serviceMetaInfoJson.getBytes(), ServiceMetaInfo.class);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toList());
            // Write to cache
            registryServiceCache.writeCache(serviceKey, serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (NacosException e) {
            LOGGER.error("Failed to discover service from Nacos", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        try {
            if (namingService != null) {
                namingService.shutDown();
            }
        } catch (NacosException e) {
            LOGGER.error("Failed to destroy Nacos registry", e);
        }
    }

    @Override
    public void heartbeat() {
        // Nacos client handles heartbeat automatically
    }

    @Override
    public void watch(String serviceKey) {
        try {
            namingService.subscribe(serviceKey, event -> {
                LOGGER.info("Nacos service changed, clearing cache for service: " + serviceKey);
                registryServiceCache.clearCache();
            });
        } catch (NacosException e) {
            LOGGER.error("Failed to subscribe to Nacos service", e);
        }
    }
} 