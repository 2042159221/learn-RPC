package com.ming.rpc.registry;

import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class SqliteRegistryTest {

    private SqliteRegistry sqliteRegistry;

    private final ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();

    @Before
    public void setUp() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("jdbc:sqlite::memory:");
        
        sqliteRegistry = new SqliteRegistry();
        sqliteRegistry.init(registryConfig);

        serviceMetaInfo.setServiceName("testService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);
        serviceMetaInfo.setServiceGroup("testGroup");
    }

    @After
    public void tearDown() {
        sqliteRegistry.destroy();
    }

    @Test
    public void testRegisterAndDiscovery() throws Exception {
        sqliteRegistry.register(serviceMetaInfo);
        List<ServiceMetaInfo> result = sqliteRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertFalse(result.isEmpty());
        assertEquals(serviceMetaInfo.getServiceHost(), result.get(0).getServiceHost());
    }

    @Test
    public void testUnregister() throws Exception {
        sqliteRegistry.register(serviceMetaInfo);
        sqliteRegistry.unregister(serviceMetaInfo);
        List<ServiceMetaInfo> result = sqliteRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertEquals(0, result.size());
    }

    @Test
    public void testHeartbeat() throws Exception {
        sqliteRegistry.register(serviceMetaInfo);
        sqliteRegistry.heartbeat();
        List<ServiceMetaInfo> result = sqliteRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertFalse(result.isEmpty());
    }
} 