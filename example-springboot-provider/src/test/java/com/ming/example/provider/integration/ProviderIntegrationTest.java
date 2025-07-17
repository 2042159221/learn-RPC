package com.ming.example.provider.integration;

import com.ming.example.common.model.User;
import com.ming.example.common.service.UserService;
import com.ming.example.provider.service.UserServiceImpl;
import com.ming.rpc.springboot.annotation.RpcService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Provider集成测试
 * 
 * 验证RPC服务提供者的完整功能
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@SpringBootTest
@TestPropertySource(properties = {
    "rpc.name=test-provider",
    "rpc.serverPort=8090",
    "rpc.registryConfig.registry=ETCD",
    "rpc.registryConfig.address=http://localhost:2380"
})
class ProviderIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Test
    void testProviderServiceRegistration() {
        // 验证服务实现类被正确注册为Spring Bean
        assertThat(userServiceImpl).isNotNull();
        assertThat(userServiceImpl).isInstanceOf(UserService.class);
        
        // 验证@RpcService注解存在
        RpcService rpcService = UserServiceImpl.class.getAnnotation(RpcService.class);
        assertThat(rpcService).isNotNull();
    }

    @Test
    void testProviderServiceFunctionality() {
        // 测试服务的实际功能
        User inputUser = new User();
        inputUser.setName("integration-test-user");

        User result = userServiceImpl.getUser(inputUser);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Provider processed: integration-test-user");
    }

    @Test
    void testSpringContextLoaded() {
        // 验证Spring上下文正确加载
        assertThat(applicationContext).isNotNull();
        
        // 验证关键Bean存在
        assertThat(applicationContext.containsBean("userServiceImpl")).isTrue();
    }

    @Test
    void testRpcConfigurationLoaded() {
        // 验证RPC配置正确加载
        // 这里主要验证应用能正常启动，说明配置没有问题
        assertThat(applicationContext.getEnvironment().getProperty("rpc.name")).isEqualTo("test-provider");
        assertThat(applicationContext.getEnvironment().getProperty("rpc.serverPort")).isEqualTo("8090");
    }
}
