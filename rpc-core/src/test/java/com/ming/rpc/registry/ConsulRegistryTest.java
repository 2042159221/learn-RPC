package com.ming.rpc.registry;

import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.consul.ConsulContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConsulRegistryTest {

    @Rule
    public ConsulContainer consulContainer = new ConsulContainer(DockerImageName.parse("consul:1.15.1"));

    private ConsulRegistry consulRegistry;

    private final ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();

    @Before
    public void setUp() {
        consulContainer.start();
        RegistryConfig registryConfig = new RegistryConfig();
        String address = consulContainer.getHost() + ":" + consulContainer.getMappedPort(8500);
        registryConfig.setAddress(address);

        consulRegistry = new ConsulRegistry();
        consulRegistry.init(registryConfig);

        serviceMetaInfo.setServiceName("testConsulService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);
        serviceMetaInfo.setServiceGroup("testGroup");
    }

    @After
    public void tearDown() {
        consulRegistry.destroy();
    }

    @Test
    public void testRegisterAndDiscovery() throws Exception {
        consulRegistry.register(serviceMetaInfo);

        // Consul registration can take a moment
        Thread.sleep(1000);

        List<ServiceMetaInfo> result = consulRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertFalse(result.isEmpty());
        assertEquals(serviceMetaInfo.getServiceHost(), result.get(0).getServiceHost());
    }

    @Test
    public void testUnregister() throws Exception {
        consulRegistry.register(serviceMetaInfo);
        Thread.sleep(1000);

        consulRegistry.unregister(serviceMetaInfo);
        Thread.sleep(1000);

        List<ServiceMetaInfo> result = consulRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertTrue(result.isEmpty());
    }
} 