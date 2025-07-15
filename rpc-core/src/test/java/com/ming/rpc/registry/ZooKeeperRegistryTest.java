package com.ming.rpc.registry;

import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;
import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ZooKeeperRegistryTest {

    private TestingServer testingServer;
    private ZooKeeperRgistry zooKeeperRegistry;

    @Before
    public void setUp() throws Exception {
        // 使用内存中的 ZooKeeper 服务器进行测试
        testingServer = new TestingServer();
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(testingServer.getConnectString());
        registryConfig.setTimeout(1000L);
        zooKeeperRegistry = new ZooKeeperRgistry();
        zooKeeperRegistry.init(registryConfig);
    }

    @After
    public void tearDown() throws Exception {
        zooKeeperRegistry.destroy();
        testingServer.close();
    }

    @Test
    public void testRegisterAndUnregister() throws Exception {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);

        // 注册服务
        zooKeeperRegistry.register(serviceMetaInfo);
        // 等待事件传播
        Thread.sleep(500);

        // 发现服务
        List<ServiceMetaInfo> serviceList = zooKeeperRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertNotNull(serviceList);
        assertEquals(1, serviceList.size());
        assertEquals(serviceMetaInfo, serviceList.get(0));

        // 注销服务
        zooKeeperRegistry.unregister(serviceMetaInfo);
        // 等待事件传播
        Thread.sleep(500);

        // 再次发现服务，应为空
        List<ServiceMetaInfo> emptyServiceList = zooKeeperRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertEquals(0, emptyServiceList.size());
    }

    @Test
    public void testMultipleNodes() throws Exception {
        // Register node 1
        ServiceMetaInfo serviceMetaInfo1 = new ServiceMetaInfo();
        serviceMetaInfo1.setServiceName("myService");
        serviceMetaInfo1.setServiceVersion("1.0");
        serviceMetaInfo1.setServiceHost("localhost");
        serviceMetaInfo1.setServicePort(1234);
        zooKeeperRegistry.register(serviceMetaInfo1);

        // Register node 2
        ServiceMetaInfo serviceMetaInfo2 = new ServiceMetaInfo();
        serviceMetaInfo2.setServiceName("myService");
        serviceMetaInfo2.setServiceVersion("1.0");
        serviceMetaInfo2.setServiceHost("localhost");
        serviceMetaInfo2.setServicePort(1235);
        zooKeeperRegistry.register(serviceMetaInfo2);

        Thread.sleep(500);

        // Discover and check
        List<ServiceMetaInfo> serviceList = zooKeeperRegistry.serviceDiscovery(serviceMetaInfo1.getServiceKey());
        assertEquals(2, serviceList.size());

        // Unregister node 1
        zooKeeperRegistry.unregister(serviceMetaInfo1);
        Thread.sleep(500);

        // Discover and check
        serviceList = zooKeeperRegistry.serviceDiscovery(serviceMetaInfo1.getServiceKey());
        assertEquals(1, serviceList.size());
        assertEquals(serviceMetaInfo2, serviceList.get(0));

        // Unregister node 2
        zooKeeperRegistry.unregister(serviceMetaInfo2);
        Thread.sleep(500);

        // Discover and check
        serviceList = zooKeeperRegistry.serviceDiscovery(serviceMetaInfo1.getServiceKey());
        assertEquals(0, serviceList.size());
    }
} 