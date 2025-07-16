package com.ming.rpc.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Mock 服务代理（用于测试）
 * 
 * @author ming
 */
public class MockServiceProxy implements InvocationHandler {

    /**
     * 调用代理
     * 
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 根据方法的返回值类型，生成默认返回值
        Class<?> returnType = method.getReturnType();
        
        // 基本类型
        if (returnType == boolean.class || returnType == Boolean.class) {
            return false;
        }
        if (returnType == int.class || returnType == Integer.class) {
            return 0;
        }
        if (returnType == long.class || returnType == Long.class) {
            return 0L;
        }
        if (returnType == byte.class || returnType == Byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class || returnType == Short.class) {
            return (short) 0;
        }
        if (returnType == float.class || returnType == Float.class) {
            return 0.0f;
        }
        if (returnType == double.class || returnType == Double.class) {
            return 0.0d;
        }
        if (returnType == char.class || returnType == Character.class) {
            return '\u0000';
        }
        if (returnType == String.class) {
            return "";
        }
        
        // 对象类型，返回null
        return null;
    }
} 