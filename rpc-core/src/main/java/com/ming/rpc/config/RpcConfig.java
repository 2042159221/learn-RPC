package com.ming.rpc.config;

import com.ming.rpc.fault.retry.RetryStrategyKeys;
import com.ming.rpc.fault.tolerant.TolerantStrategyKeys;
import com.ming.rpc.loadbalancer.LoadBalancerKeys;
import com.ming.rpc.serializer.SerializerKeys;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC 框架配置
 */
@Data
public class RpcConfig {

    /**
     * 名称
     */
    private String name = "rpc";

    /**
     * 版本号
     */
    private String version = "1.0";

    /**
     * 服务器主机名
     */
    private String serverHost = "localhost";

    /**
     * 服务器端口号
     */
    private Integer serverPort = 8080;

    /**
     * 序列化器
     */
    private String serializer = SerializerKeys.JDK;

    /**
     * 负载均衡器
     */
    private String loadBalancer = LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     */
    private String retryStrategy = RetryStrategyKeys.NO;

    /**
     * 容错策略
     */
    private String tolerantStrategy = TolerantStrategyKeys.FAIL_FAST;

    /**
     * 模拟调用
     */
    private boolean mock = false;

    /**
     * Mock服务注册表
     */
    private final Map<String, Class<?>> mockServiceRegistry = new HashMap<>();

    /**
     * 注册中心配置
     */
    private RegistryConfig registryConfig = new RegistryConfig();
} 