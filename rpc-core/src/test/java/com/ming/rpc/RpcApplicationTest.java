package com.ming.rpc;

import com.ming.rpc.config.RpcConfig;
import org.junit.Assert;
import org.junit.Test;

/**
 * RPC应用测试类
 */
public class RpcApplicationTest {

    /**
     * 测试RpcApplication的单例模式
     */
    @Test
    public void testSingleton() {
        // 获取RPC配置实例
        RpcConfig config1 = RpcApplication.getRpcConfig();
        RpcConfig config2 = RpcApplication.getRpcConfig();
        
        // 验证两次获取的是同一个实例
        Assert.assertSame(config1, config2);
    }
    
    /**
     * 测试初始化方法
     */
    @Test
    public void testInit() {
        // 使用默认配置初始化
        RpcApplication.init();
        
        // 获取配置并验证
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Assert.assertNotNull(rpcConfig);
        
        // 验证从配置文件加载的值
        // 注意：这个测试依赖于application.properties中的配置
        Assert.assertEquals("ming-rpc", rpcConfig.getName());
    }
    
    /**
     * 测试自定义配置初始化
     */
    @Test
    public void testCustomInit() {
        // 创建自定义配置
        RpcConfig customConfig = new RpcConfig();
        customConfig.setName("custom-rpc");
        customConfig.setVersion("2.0");
        customConfig.setServerPort(8888);
        
        // 使用自定义配置初始化
        RpcApplication.init(customConfig);
        
        // 获取配置并验证
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Assert.assertNotNull(rpcConfig);
        
        // 验证自定义配置值
        Assert.assertEquals("custom-rpc", rpcConfig.getName());
        Assert.assertEquals("2.0", rpcConfig.getVersion());
        Assert.assertEquals(Integer.valueOf(8888), rpcConfig.getServerPort());
    }
} 