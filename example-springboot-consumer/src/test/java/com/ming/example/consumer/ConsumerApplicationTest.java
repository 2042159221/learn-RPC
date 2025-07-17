package com.ming.example.consumer;

import com.ming.example.consumer.controller.UserController;
import com.ming.rpc.springboot.annotation.EnableRpc;
import com.ming.rpc.springboot.annotation.RpcReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consumer应用启动测试
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
class ConsumerApplicationTest {

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
        EnableRpc enableRpc = ConsumerApplication.class.getAnnotation(EnableRpc.class);
        assertThat(enableRpc).isNotNull();
        assertThat(enableRpc.needServer()).isFalse();
    }

    @Test
    void testUserControllerBean() {
        // 验证UserController Bean被正确注册
        assertThat(applicationContext.containsBean("userController")).isTrue();
        
        UserController userController = applicationContext.getBean(UserController.class);
        assertThat(userController).isNotNull();
    }

    @Test
    void testRpcReferenceAnnotation() throws NoSuchFieldException {
        // 验证@RpcReference注解存在
        Field userServiceField = UserController.class.getDeclaredField("userService");
        RpcReference rpcReference = userServiceField.getAnnotation(RpcReference.class);
        assertThat(rpcReference).isNotNull();
    }

    @Test
    void testUserControllerFieldType() throws NoSuchFieldException {
        // 验证UserController中的userService字段类型正确
        Field userServiceField = UserController.class.getDeclaredField("userService");
        assertThat(userServiceField.getType().getName()).isEqualTo("com.ming.example.common.service.UserService");
    }
}
