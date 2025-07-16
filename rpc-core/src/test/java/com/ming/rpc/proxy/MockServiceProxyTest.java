package com.ming.rpc.proxy;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

/**
 * MockServiceProxy 单元测试
 */
public class MockServiceProxyTest {

    /**
     * 测试服务接口定义
     */
    private interface TestService {
        String hello(String name);
        int add(int a, int b);
        boolean isValid();
        long getValue();
        double getPrice();
        float getRate();
        byte getByte();
        short getShort();
        char getChar();
        TestObject getObject();
    }
    
    /**
     * 测试对象类
     */
    private static class TestObject {
        private String name;
        
        public String getName() {
            return name;
        }
    }

    private final MockServiceProxy mockServiceProxy = new MockServiceProxy();
    
    /**
     * 测试字符串类型返回值
     */
    @Test
    public void testStringReturn() throws Throwable {
        Method method = TestService.class.getMethod("hello", String.class);
        Object result = mockServiceProxy.invoke(null, method, new Object[]{"test"});
        assertEquals("", result);
    }
    
    /**
     * 测试整数类型返回值
     */
    @Test
    public void testIntReturn() throws Throwable {
        Method method = TestService.class.getMethod("add", int.class, int.class);
        Object result = mockServiceProxy.invoke(null, method, new Object[]{5, 10});
        assertEquals(0, result);
    }
    
    /**
     * 测试布尔类型返回值
     */
    @Test
    public void testBooleanReturn() throws Throwable {
        Method method = TestService.class.getMethod("isValid");
        Object result = mockServiceProxy.invoke(null, method, null);
        assertEquals(false, result);
    }
    
    /**
     * 测试长整型返回值
     */
    @Test
    public void testLongReturn() throws Throwable {
        Method method = TestService.class.getMethod("getValue");
        Object result = mockServiceProxy.invoke(null, method, null);
        assertEquals(0L, result);
    }
    
    /**
     * 测试双精度返回值
     */
    @Test
    public void testDoubleReturn() throws Throwable {
        Method method = TestService.class.getMethod("getPrice");
        Object result = mockServiceProxy.invoke(null, method, null);
        assertEquals(0.0d, result);
    }
    
    /**
     * 测试单精度返回值
     */
    @Test
    public void testFloatReturn() throws Throwable {
        Method method = TestService.class.getMethod("getRate");
        Object result = mockServiceProxy.invoke(null, method, null);
        assertEquals(0.0f, result);
    }
    
    /**
     * 测试字节返回值
     */
    @Test
    public void testByteReturn() throws Throwable {
        Method method = TestService.class.getMethod("getByte");
        Object result = mockServiceProxy.invoke(null, method, null);
        assertEquals((byte)0, result);
    }
    
    /**
     * 测试短整型返回值
     */
    @Test
    public void testShortReturn() throws Throwable {
        Method method = TestService.class.getMethod("getShort");
        Object result = mockServiceProxy.invoke(null, method, null);
        assertEquals((short)0, result);
    }
    
    /**
     * 测试字符返回值
     */
    @Test
    public void testCharReturn() throws Throwable {
        Method method = TestService.class.getMethod("getChar");
        Object result = mockServiceProxy.invoke(null, method, null);
        assertEquals('\u0000', result);
    }
    
    /**
     * 测试对象返回值
     */
    @Test
    public void testObjectReturn() throws Throwable {
        Method method = TestService.class.getMethod("getObject");
        Object result = mockServiceProxy.invoke(null, method, null);
        assertNull(result);
    }
} 