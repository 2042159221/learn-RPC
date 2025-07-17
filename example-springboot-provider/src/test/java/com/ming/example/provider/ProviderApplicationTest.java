package com.ming.example.provider;

import com.ming.example.common.service.UserService;
import com.ming.example.provider.service.UserServiceImpl;
import com.ming.rpc.springboot.annotation.EnableRpc;
import com.ming.rpc.springboot.annotation.RpcService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Provider应用启动测试
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@SpringBootTest
@TestPropertySource(properties = {
    "rpc.registryConfig.registry=ETCD",
    "rpc.registryConfig.address=http://localhost:2380"
})
class ProviderApplicationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        // 验证Spring上下文正常加载
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void testEnableRpcAnnotation() {
        // 验证@EnableRpc注解存在
        EnableRpc enableRpc = ProviderApplication.class.getAnnotation(EnableRpc.class);
        assertThat(enableRpc).isNotNull();
        assertThat(enableRpc.needServer()).isTrue();
    }

    @Test
    void testUserServiceBean() {
        // 验证UserService Bean被正确注册
        assertThat(applicationContext.containsBean("userServiceImpl")).isTrue();
        
        UserServiceImpl userService = applicationContext.getBean(UserServiceImpl.class);
        assertThat(userService).isNotNull();
        assertThat(userService).isInstanceOf(UserService.class);
    }

    @Test
    void testRpcServiceAnnotation() {
        // 验证@RpcService注解存在
        RpcService rpcService = UserServiceImpl.class.getAnnotation(RpcService.class);
        assertThat(rpcService).isNotNull();
    }

    @Test
    void testUserServiceImplementsInterface() {
        // 验证UserServiceImpl实现了UserService接口
        assertThat(UserService.class.isAssignableFrom(UserServiceImpl.class)).isTrue();
    }
}
