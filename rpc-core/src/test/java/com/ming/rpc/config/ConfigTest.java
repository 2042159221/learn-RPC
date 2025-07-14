package com.ming.rpc.config;

import com.ming.rpc.constant.RpcConstant;
import com.ming.rpc.utils.ConfigUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * 配置加载测试类
 */
public class ConfigTest {

    /**
     * 测试默认配置加载
     */
    @Test
    public void testDefaultConfig() {
        // 创建默认配置对象
        RpcConfig rpcConfig = new RpcConfig();
        
        // 验证默认值
        Assert.assertEquals("rpc", rpcConfig.getName());
        Assert.assertEquals("1.0", rpcConfig.getVersion());
        Assert.assertEquals("localhost", rpcConfig.getServerHost());
        Assert.assertEquals(Integer.valueOf(8080), rpcConfig.getServerPort());
        Assert.assertFalse(rpcConfig.isMock());
        
        // 验证注册中心默认配置
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Assert.assertNotNull(registryConfig);
        Assert.assertEquals("etcd", registryConfig.getRegistry());
        Assert.assertEquals("http://localhost:2380", registryConfig.getAddress());
        Assert.assertEquals(Long.valueOf(10000), registryConfig.getTimeout());
    }
    
    /**
     * 测试从配置文件加载配置
     */
    @Test
    public void testLoadFromFile() {
        // 从默认配置文件加载
        RpcConfig rpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX);
        
        // 验证加载的值
        Assert.assertEquals("ming-rpc", rpcConfig.getName());
        Assert.assertEquals("1.0", rpcConfig.getVersion());
        Assert.assertEquals("localhost", rpcConfig.getServerHost());
        Assert.assertEquals(Integer.valueOf(9000), rpcConfig.getServerPort());
        Assert.assertTrue(rpcConfig.isMock());
        
        // 验证注册中心配置
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Assert.assertNotNull(registryConfig);
        Assert.assertEquals("etcd", registryConfig.getRegistry().toLowerCase());
        Assert.assertEquals("http://localhost:2380", registryConfig.getAddress());
        Assert.assertEquals(Long.valueOf(5000), registryConfig.getTimeout());
    }
    
    /**
     * 测试从特定环境配置文件加载配置
     */
    @Test
    public void testLoadFromEnvironmentFile() {
        // 从开发环境配置文件加载
        RpcConfig rpcConfig = ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX, "dev");
        
        // 验证加载的值
        Assert.assertEquals("ming-rpc-dev", rpcConfig.getName());
        Assert.assertEquals("1.0-dev", rpcConfig.getVersion());
        Assert.assertEquals("localhost", rpcConfig.getServerHost());
        Assert.assertEquals(Integer.valueOf(8081), rpcConfig.getServerPort());
        Assert.assertEquals("JSON", rpcConfig.getSerializer());
        Assert.assertTrue(rpcConfig.isMock());
        
        // 验证注册中心配置
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        Assert.assertNotNull(registryConfig);
        Assert.assertEquals("local", registryConfig.getRegistry().toLowerCase());
        // 使用 trim() 方法去除可能存在于地址字符串前后的多余空格
        Assert.assertEquals("http://localhost:8848", registryConfig.getAddress().trim());
    }
    
    /**
     * 测试异常情况下的配置加载
     */
    @Test
    public void testLoadWithException() {
        // 尝试加载不存在的环境配置,并断言会抛出异常
        Assert.assertThrows(cn.hutool.core.io.resource.NoResourceException.class, () -> {
            ConfigUtils.loadConfig(RpcConfig.class, RpcConstant.DEFAULT_CONFIG_PREFIX, "notExist");
        });
    }
} 