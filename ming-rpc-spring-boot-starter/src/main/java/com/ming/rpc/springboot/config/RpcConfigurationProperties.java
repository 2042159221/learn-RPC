package com.ming.rpc.springboot.config;

import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.config.RpcConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * RPC配置属性类
 * 
 * 用于绑定Spring Boot配置文件中的rpc.*配置项
 * 复用现有的RpcConfig和RegistryConfig结构
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@Data
@ConfigurationProperties(prefix = "rpc")
public class RpcConfigurationProperties {

    /**
     * 框架名称
     */
    private String name = "ming-rpc";

    /**
     * 框架版本
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
     * 序列化器类型
     */
    private String serializer = "JDK";

    /**
     * 负载均衡器类型
     */
    private String loadBalancer = "ROUND_ROBIN";

    /**
     * 重试策略
     */
    private String retryStrategy = "NO";

    /**
     * 容错策略
     */
    private String tolerantStrategy = "FAIL_FAST";

    /**
     * 是否启用模拟调用
     */
    private boolean mock = false;

    /**
     * 注册中心配置
     */
    @NestedConfigurationProperty
    private RegistryConfigProperties registryConfig = new RegistryConfigProperties();

    /**
     * 转换为RpcConfig对象
     * 
     * @return RpcConfig实例
     */
    public RpcConfig toRpcConfig() {
        RpcConfig rpcConfig = new RpcConfig();
        rpcConfig.setName(this.name);
        rpcConfig.setVersion(this.version);
        rpcConfig.setServerHost(this.serverHost);
        rpcConfig.setServerPort(this.serverPort);
        rpcConfig.setSerializer(this.serializer);
        rpcConfig.setLoadBalancer(this.loadBalancer);
        rpcConfig.setRetryStrategy(this.retryStrategy);
        rpcConfig.setTolerantStrategy(this.tolerantStrategy);
        rpcConfig.setMock(this.mock);
        rpcConfig.setRegistryConfig(this.registryConfig.toRegistryConfig());
        return rpcConfig;
    }

    /**
     * 注册中心配置属性
     */
    @Data
    public static class RegistryConfigProperties {

        /**
         * 注册中心类型
         */
        private String registry = "ETCD";

        /**
         * 注册中心地址
         */
        private String address = "http://localhost:2380";

        /**
         * 用户名
         */
        private String username;

        /**
         * 密码
         */
        private String password;

        /**
         * 超时时间（毫秒）
         */
        private Long timeout = 10000L;

        /**
         * 转换为RegistryConfig对象
         * 
         * @return RegistryConfig实例
         */
        public RegistryConfig toRegistryConfig() {
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setRegistry(this.registry);
            registryConfig.setAddress(this.address);
            registryConfig.setUsername(this.username);
            registryConfig.setPassword(this.password);
            registryConfig.setTimeout(this.timeout);
            return registryConfig;
        }
    }
}
