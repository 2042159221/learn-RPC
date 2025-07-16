package com.ming.rpc.server.http;

/**
 * 测试服务实现类
 */
public class TestServiceImpl implements TestService {
    
    @Override
    public String echo(String input) {
        return input;
    }
    
    @Override
    public int add(int a, int b) {
        return a + b;
    }
} 