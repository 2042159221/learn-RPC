package com.ming.rpc.registry;

import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PostgreSqlRegistryTest {

    @Rule
    public PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.3");

    private PostgreSqlRegistry postgreSqlRegistry;

    private final ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();

    @Before
    public void setUp() {
        postgreSQLContainer.start();
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(postgreSQLContainer.getJdbcUrl());
        registryConfig.setUsername(postgreSQLContainer.getUsername());
        registryConfig.setPassword(postgreSQLContainer.getPassword());

        postgreSqlRegistry = new PostgreSqlRegistry();
        postgreSqlRegistry.init(registryConfig);

        serviceMetaInfo.setServiceName("testService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);
        serviceMetaInfo.setServiceGroup("testGroup");
    }

    @After
    public void tearDown() {
        postgreSqlRegistry.destroy();
    }

    @Test
    public void testRegisterAndDiscovery() throws Exception {
        postgreSqlRegistry.register(serviceMetaInfo);
        List<ServiceMetaInfo> result = postgreSqlRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertFalse(result.isEmpty());
        assertEquals(serviceMetaInfo.getServiceHost(), result.get(0).getServiceHost());
    }

    @Test
    public void testUnregister() throws Exception {
        postgreSqlRegistry.register(serviceMetaInfo);
        postgreSqlRegistry.unregister(serviceMetaInfo);
        List<ServiceMetaInfo> result = postgreSqlRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertEquals(0, result.size());
    }

    @Test
    public void testHeartbeat() throws Exception {
        postgreSqlRegistry.register(serviceMetaInfo);
        postgreSqlRegistry.heartbeat();
        List<ServiceMetaInfo> result = postgreSqlRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertFalse(result.isEmpty());
    }
} 