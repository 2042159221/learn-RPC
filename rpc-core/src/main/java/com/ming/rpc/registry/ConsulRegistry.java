package com.ming.rpc.registry;

import com.google.common.net.HostAndPort;
import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;
import com.ming.rpc.serializer.Serializer;
import com.ming.rpc.serializer.SerializerFactory;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import org.kiwiproject.consul.Consul;
import org.kiwiproject.consul.HealthClient;
import org.kiwiproject.consul.model.agent.ImmutableRegistration;
import org.kiwiproject.consul.model.agent.Registration;
import org.kiwiproject.consul.model.health.ServiceHealth;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsulRegistry implements Registry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsulRegistry.class);

    private Consul consulClient;
    private HealthClient healthClient;
    private final Serializer serializer = SerializerFactory.getInstance("json");
    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();

    @Override
    public void init(RegistryConfig registryConfig) {
        HostAndPort hostAndPort = HostAndPort.fromString(registryConfig.getAddress());
        this.consulClient = Consul.builder().withHostAndPort(hostAndPort).build();
        this.healthClient = consulClient.healthClient();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        Registration registration = ImmutableRegistration.builder()
                .id(serviceMetaInfo.getServiceNodeKey())
                .name(serviceMetaInfo.getServiceKey())
                .address(serviceMetaInfo.getServiceHost())
                .port(serviceMetaInfo.getServicePort())
                .meta(Collections.singletonMap("serviceMetaInfo", new String(serializer.serialize(serviceMetaInfo))))
                .build();
        consulClient.agentClient().register(registration);
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        consulClient.agentClient().deregister(serviceMetaInfo.getServiceNodeKey());
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        List<ServiceMetaInfo> cachedServices = registryServiceCache.readCache(serviceKey);
        if (cachedServices != null && !cachedServices.isEmpty()) {
            return cachedServices;
        }

        List<ServiceHealth> serviceHealthList = healthClient.getHealthyServiceInstances(serviceKey).getResponse();
        List<ServiceMetaInfo> serviceMetaInfoList = serviceHealthList.stream()
                .map(serviceHealth -> {
                    Map<String, String> meta = serviceHealth.getService().getMeta();
                    try {
                        return serializer.deserialize(meta.get("serviceMetaInfo").getBytes(), ServiceMetaInfo.class);
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to deserialize service meta info from consul", e);
                    }
                })
                .collect(Collectors.toList());

        registryServiceCache.writeCache(serviceKey, serviceMetaInfoList);
        return serviceMetaInfoList;
    }

    @Override
    public void destroy() {
        if (consulClient != null) {
            consulClient.destroy();
        }
    }

    @Override
    public void heartbeat() {
        // Consul agent handles heartbeat via TTL checks
    }

    @Override
    public void watch(String serviceKey) {
        // Consul doesn't have a direct watch mechanism like ZK or Nacos.
        // A common pattern is to poll for changes.
        // For this implementation, we will rely on service discovery to refresh the cache.
        // A more advanced implementation could use a scheduled task to poll and update the cache.
        LOGGER.info("Consul watch is implemented via polling on serviceDiscovery.");
    }
} 