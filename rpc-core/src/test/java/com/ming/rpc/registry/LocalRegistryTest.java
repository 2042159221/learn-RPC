package com.ming.rpc.registry;

import org.junit.Assert;
import org.junit.Test;

/**
 * 本地注册中心测试
 */
public class LocalRegistryTest {

    @Test
    public void testRegisterAndGet() {
        // 定义服务名称和实现类
        String serviceName = "myService";
        class MyService {}
        Class<?> myServiceClass = MyService.class;

        // 注册服务
        LocalRegistry.register(serviceName, myServiceClass);

        // 获取服务
        Class<?> retrievedServiceClass = LocalRegistry.get(serviceName);

        // 验证
        Assert.assertNotNull("获取的服务不应为 null", retrievedServiceClass);
        Assert.assertEquals("获取的服务类与注册的服务类不一致", myServiceClass, retrievedServiceClass);
    }
    
    @Test
    public void testRemove() {
        // 定义服务名称和实现类
        String serviceName = "anotherService";
        class AnotherService {}
        Class<?> anotherServiceClass = AnotherService.class;

        // 先注册
        LocalRegistry.register(serviceName, anotherServiceClass);
        Assert.assertNotNull("注册后获取服务不应为 null", LocalRegistry.get(serviceName));
        
        // 再移除
        LocalRegistry.remove(serviceName);
        
        // 验证
        Class<?> retrievedServiceClass = LocalRegistry.get(serviceName);
        Assert.assertNull("移除后获取的服务应为 null", retrievedServiceClass);
    }
} 