package com.ming.rpc.proxy;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ming.rpc.RpcApplication;
import com.ming.rpc.config.RpcConfig;

/**
 * ServiceProxyFactory 单元测试
 */
public class ServiceProxyFactoryTest {

    /**
     * 测试服务接口定义
     */
    private interface TestService {
        String hello(String name);
        int add(int a, int b);
    }
    
    @Before
    public void setUp() {
        // 初始化RPC配置
        RpcConfig rpcConfig = new RpcConfig();
        rpcConfig.setName("test-rpc");
        RpcApplication.init(rpcConfig);
    }
    
    @After
    public void tearDown() {
        RpcApplication.destroy();
    }
    
    /**
     * 测试正常模式下获取代理对象
     */
    @Test
    public void testGetProxyNonMockMode() {
        // 设置为非mock模式
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        rpcConfig.setMock(false);
        
        // 获取代理
        TestService testService = ServiceProxyFactory.getProxy(TestService.class);
        
        // 验证代理对象不为空，并且是代理类实例
        assertNotNull("代理对象不应为null", testService);
        assertTrue("应该是代理类实例", java.lang.reflect.Proxy.isProxyClass(testService.getClass()));
    }
    
    /**
     * 测试mock模式下获取代理对象
     */
    @Test
    public void testGetProxyMockMode() {
        // 设置为mock模式
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        rpcConfig.setMock(true);
        
        // 获取代理
        TestService testService = ServiceProxyFactory.getProxy(TestService.class);
        
        // 验证代理对象不为空，并且是代理类实例
        assertNotNull("代理对象不应为null", testService);
        assertTrue("应该是代理类实例", java.lang.reflect.Proxy.isProxyClass(testService.getClass()));
        
        // 在mock模式下，调用应该返回默认值
        assertEquals("", testService.hello("world"));
        assertEquals(0, testService.add(5, 10));
    }
    
    /**
     * 测试直接获取Mock代理
     */
    @Test
    public void testGetMockProxy() {
        // 直接获取mock代理
        TestService testService = ServiceProxyFactory.getMockProxy(TestService.class);
        
        // 验证代理对象不为空，并且是代理类实例
        assertNotNull("代理对象不应为null", testService);
        assertTrue("应该是代理类实例", java.lang.reflect.Proxy.isProxyClass(testService.getClass()));
        
        // mock代理应该返回默认值
        assertEquals("", testService.hello("world"));
        assertEquals(0, testService.add(5, 10));
    }
} 