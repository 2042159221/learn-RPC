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
    public void testRegisterAndDiscovery() throws Exception {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);

        // 注册服务
        zooKeeperRegistry.register(serviceMetaInfo);
        Thread.sleep(200);

        // 发现服务
        List<ServiceMetaInfo> serviceList = zooKeeperRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertNotNull(serviceList);
        assertEquals(1, serviceList.size());
        assertEquals(serviceMetaInfo, serviceList.get(0));
    }

    @Test
    public void testUnregister() throws Exception {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);

        // 先注册
        zooKeeperRegistry.register(serviceMetaInfo);
        Thread.sleep(200);

        // 再注销
        zooKeeperRegistry.unregister(serviceMetaInfo);
        Thread.sleep(200);

        // 发现服务，应该为空
        List<ServiceMetaInfo> serviceList = zooKeeperRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertEquals(0, serviceList.size());
    }

    @Test
    public void testWatch() throws Exception {
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("myService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);

        // 注册服务
        zooKeeperRegistry.register(serviceMetaInfo);
        Thread.sleep(200);

        // 首次发现，写入缓存
        List<ServiceMetaInfo> serviceList = zooKeeperRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertEquals(1, serviceList.size());

        // 监听
        zooKeeperRegistry.watch(serviceMetaInfo.getServiceKey());

        // 注销服务，触发监听器清空缓存
        zooKeeperRegistry.unregister(serviceMetaInfo);

        // 等待事件传播
        Thread.sleep(1000);

        // 再次发现，由于缓存被清空，应该从 ZK 获取，结果为空
        List<ServiceMetaInfo> updatedServiceList = zooKeeperRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertEquals(0, updatedServiceList.size());
    }
} 