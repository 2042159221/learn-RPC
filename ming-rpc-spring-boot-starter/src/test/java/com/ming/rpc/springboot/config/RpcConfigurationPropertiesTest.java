package com.ming.rpc.springboot.config;

import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.config.RpcConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RpcConfigurationProperties测试类
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
class RpcConfigurationPropertiesTest {

    @Test
    void testDefaultValues() {
        RpcConfigurationProperties properties = new RpcConfigurationProperties();
        
        // 验证默认值
        assertThat(properties.getName()).isEqualTo("ming-rpc");
        assertThat(properties.getVersion()).isEqualTo("1.0");
        assertThat(properties.getServerHost()).isEqualTo("localhost");
        assertThat(properties.getServerPort()).isEqualTo(8080);
        assertThat(properties.getSerializer()).isEqualTo("JDK");
        assertThat(properties.getLoadBalancer()).isEqualTo("ROUND_ROBIN");
        assertThat(properties.getRetryStrategy()).isEqualTo("NO");
        assertThat(properties.getTolerantStrategy()).isEqualTo("FAIL_FAST");
        assertThat(properties.isMock()).isFalse();
        
        // 验证注册中心默认值
        RpcConfigurationProperties.RegistryConfigProperties registryConfig = properties.getRegistryConfig();
        assertThat(registryConfig.getRegistry()).isEqualTo("ETCD");
        assertThat(registryConfig.getAddress()).isEqualTo("http://localhost:2380");
        assertThat(registryConfig.getTimeout()).isEqualTo(10000L);
    }

    @Test
    void testPropertyBinding() {
        // 准备配置数据
        Map<String, Object> properties = new HashMap<>();
        properties.put("rpc.name", "test-rpc");
        properties.put("rpc.version", "2.0");
        properties.put("rpc.server-host", "127.0.0.1");
        properties.put("rpc.server-port", "9999");
        properties.put("rpc.serializer", "KRYO");
        properties.put("rpc.load-balancer", "RANDOM");
        properties.put("rpc.retry-strategy", "FIXED_INTERVAL");
        properties.put("rpc.tolerant-strategy", "FAIL_OVER");
        properties.put("rpc.mock", "true");
        properties.put("rpc.registry-config.registry", "ZOOKEEPER");
        properties.put("rpc.registry-config.address", "localhost:2181");
        properties.put("rpc.registry-config.username", "admin");
        properties.put("rpc.registry-config.password", "password");
        properties.put("rpc.registry-config.timeout", "5000");

        // 创建配置源和绑定器
        ConfigurationPropertySource source = new MapConfigurationPropertySource(properties);
        Binder binder = new Binder(source);

        // 绑定配置
        RpcConfigurationProperties rpcProperties = binder.bind("rpc", RpcConfigurationProperties.class).get();

        // 验证绑定结果
        assertThat(rpcProperties.getName()).isEqualTo("test-rpc");
        assertThat(rpcProperties.getVersion()).isEqualTo("2.0");
        assertThat(rpcProperties.getServerHost()).isEqualTo("127.0.0.1");
        assertThat(rpcProperties.getServerPort()).isEqualTo(9999);
        assertThat(rpcProperties.getSerializer()).isEqualTo("KRYO");
        assertThat(rpcProperties.getLoadBalancer()).isEqualTo("RANDOM");
        assertThat(rpcProperties.getRetryStrategy()).isEqualTo("FIXED_INTERVAL");
        assertThat(rpcProperties.getTolerantStrategy()).isEqualTo("FAIL_OVER");
        assertThat(rpcProperties.isMock()).isTrue();

        // 验证注册中心配置绑定
        RpcConfigurationProperties.RegistryConfigProperties registryConfig = rpcProperties.getRegistryConfig();
        assertThat(registryConfig.getRegistry()).isEqualTo("ZOOKEEPER");
        assertThat(registryConfig.getAddress()).isEqualTo("localhost:2181");
        assertThat(registryConfig.getUsername()).isEqualTo("admin");
        assertThat(registryConfig.getPassword()).isEqualTo("password");
        assertThat(registryConfig.getTimeout()).isEqualTo(5000L);
    }

    @Test
    void testToRpcConfig() {
        RpcConfigurationProperties properties = new RpcConfigurationProperties();
        properties.setName("test-rpc");
        properties.setVersion("2.0");
        properties.setServerHost("127.0.0.1");
        properties.setServerPort(9999);
        properties.setSerializer("KRYO");
        properties.setLoadBalancer("RANDOM");
        properties.setRetryStrategy("FIXED_INTERVAL");
        properties.setTolerantStrategy("FAIL_OVER");
        properties.setMock(true);

        RpcConfigurationProperties.RegistryConfigProperties registryConfigProperties = 
            new RpcConfigurationProperties.RegistryConfigProperties();
        registryConfigProperties.setRegistry("ZOOKEEPER");
        registryConfigProperties.setAddress("localhost:2181");
        registryConfigProperties.setUsername("admin");
        registryConfigProperties.setPassword("password");
        registryConfigProperties.setTimeout(5000L);
        properties.setRegistryConfig(registryConfigProperties);

        // 转换为RpcConfig
        RpcConfig rpcConfig = properties.toRpcConfig();

        // 验证转换结果
        assertThat(rpcConfig.getName()).isEqualTo("test-rpc");
        assertThat(rpcConfig.getVersion()).isEqualTo("2.0");
        assertThat(rpcConfig.getServerHost()).isEqualTo("127.0.0.1");
        assertThat(rpcConfig.getServerPort()).isEqualTo(9999);
        assertThat(rpcConfig.getSerializer()).isEqualTo("KRYO");
        assertThat(rpcConfig.getLoadBalancer()).isEqualTo("RANDOM");
        assertThat(rpcConfig.getRetryStrategy()).isEqualTo("FIXED_INTERVAL");
        assertThat(rpcConfig.getTolerantStrategy()).isEqualTo("FAIL_OVER");
        assertThat(rpcConfig.isMock()).isTrue();

        // 验证注册中心配置转换
        RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
        assertThat(registryConfig.getRegistry()).isEqualTo("ZOOKEEPER");
        assertThat(registryConfig.getAddress()).isEqualTo("localhost:2181");
        assertThat(registryConfig.getUsername()).isEqualTo("admin");
        assertThat(registryConfig.getPassword()).isEqualTo("password");
        assertThat(registryConfig.getTimeout()).isEqualTo(5000L);
    }

    @Test
    void testToRegistryConfig() {
        RpcConfigurationProperties.RegistryConfigProperties properties = 
            new RpcConfigurationProperties.RegistryConfigProperties();
        properties.setRegistry("ZOOKEEPER");
        properties.setAddress("localhost:2181");
        properties.setUsername("admin");
        properties.setPassword("password");
        properties.setTimeout(5000L);

        // 转换为RegistryConfig
        RegistryConfig registryConfig = properties.toRegistryConfig();

        // 验证转换结果
        assertThat(registryConfig.getRegistry()).isEqualTo("ZOOKEEPER");
        assertThat(registryConfig.getAddress()).isEqualTo("localhost:2181");
        assertThat(registryConfig.getUsername()).isEqualTo("admin");
        assertThat(registryConfig.getPassword()).isEqualTo("password");
        assertThat(registryConfig.getTimeout()).isEqualTo(5000L);
    }
}
