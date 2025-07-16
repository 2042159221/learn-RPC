package com.ming.rpc.server.http;

/**
 * 用于测试的服务接口
 */
public interface TestService {
    
    /**
     * 测试方法，返回输入字符串
     * @param input 输入字符串
     * @return 输出字符串
     */
    String echo(String input);
    
    /**
     * 测试方法，返回两数之和
     * @param a 第一个数
     * @param b 第二个数
     * @return 两数之和
     */
    int add(int a, int b);
} 