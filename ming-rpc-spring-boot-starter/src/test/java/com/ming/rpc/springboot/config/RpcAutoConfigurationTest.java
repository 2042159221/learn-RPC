package com.ming.rpc.springboot.config;

import com.ming.rpc.RpcApplication;
import com.ming.rpc.config.RpcConfig;
import com.ming.rpc.springboot.processor.RpcBeanPostProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RpcAutoConfiguration测试类
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
class RpcAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RpcAutoConfiguration.class));

    @AfterEach
    void tearDown() {
        // 清理RpcApplication状态
        RpcApplication.destroy();
    }

    @Test
    void testAutoConfigurationEnabled() {
        contextRunner
                .withPropertyValues("rpc.enabled=true")
                .run(context -> {
                    // 验证Bean被创建
                    assertThat(context).hasSingleBean(RpcConfig.class);
                    assertThat(context).hasSingleBean(RpcBeanPostProcessor.class);
                    assertThat(context).hasSingleBean(RpcConfigurationProperties.class);
                });
    }

    @Test
    void testAutoConfigurationDisabled() {
        contextRunner
                .withPropertyValues("rpc.enabled=false")
                .run(context -> {
                    // 验证Bean未被创建
                    assertThat(context).doesNotHaveBean(RpcConfig.class);
                    assertThat(context).doesNotHaveBean(RpcBeanPostProcessor.class);
                });
    }

    @Test
    void testAutoConfigurationDefaultEnabled() {
        contextRunner
                .run(context -> {
                    // 默认情况下应该启用
                    assertThat(context).hasSingleBean(RpcConfig.class);
                    assertThat(context).hasSingleBean(RpcBeanPostProcessor.class);
                });
    }

    @Test
    void testRpcConfigWithCustomProperties() {
        contextRunner
                .withPropertyValues(
                        "rpc.name=test-rpc",
                        "rpc.version=2.0",
                        "rpc.server-host=127.0.0.1",
                        "rpc.server-port=9999"
                )
                .run(context -> {
                    RpcConfig rpcConfig = context.getBean(RpcConfig.class);
                    assertThat(rpcConfig.getName()).isEqualTo("test-rpc");
                    assertThat(rpcConfig.getVersion()).isEqualTo("2.0");
                    assertThat(rpcConfig.getServerHost()).isEqualTo("127.0.0.1");
                    assertThat(rpcConfig.getServerPort()).isEqualTo(9999);
                });
    }

    @Test
    void testRpcConfigWithDefaultProperties() {
        contextRunner
                .run(context -> {
                    RpcConfig rpcConfig = context.getBean(RpcConfig.class);
                    assertThat(rpcConfig.getName()).isEqualTo("ming-rpc");
                    assertThat(rpcConfig.getVersion()).isEqualTo("1.0");
                    assertThat(rpcConfig.getServerHost()).isEqualTo("localhost");
                    assertThat(rpcConfig.getServerPort()).isEqualTo(8080);
                });
    }

    @Test
    void testCustomRpcConfigBean() {
        contextRunner
                .withBean("customRpcConfig", RpcConfig.class, () -> {
                    RpcConfig config = new RpcConfig();
                    config.setName("custom-rpc");
                    return config;
                })
                .run(context -> {
                    // 验证自定义Bean优先级更高
                    assertThat(context).hasSingleBean(RpcConfig.class);
                    RpcConfig rpcConfig = context.getBean(RpcConfig.class);
                    assertThat(rpcConfig.getName()).isEqualTo("custom-rpc");
                });
    }
}
