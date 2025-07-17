package com.ming.example.consumer.integration;

import com.ming.example.consumer.controller.UserController;
import com.ming.rpc.springboot.annotation.RpcReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Consumer集成测试
 * 
 * 验证RPC服务消费者的完整功能
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "rpc.name=test-consumer",
    "rpc.registryConfig.registry=ETCD",
    "rpc.registryConfig.address=http://localhost:2380"
})
class ConsumerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserController userController;

    @Test
    void testConsumerServiceInjection() throws Exception {
        // 验证控制器被正确注入
        assertThat(userController).isNotNull();
        
        // 验证@RpcReference注解存在
        Field userServiceField = UserController.class.getDeclaredField("userService");
        RpcReference rpcReference = userServiceField.getAnnotation(RpcReference.class);
        assertThat(rpcReference).isNotNull();
    }

    @Test
    void testHealthEndpoint() {
        // 测试健康检查接口
        String url = "http://localhost:" + port + "/user/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Consumer is healthy");
    }

    @Test
    void testSpringContextLoaded() {
        // 验证Spring上下文正确加载
        assertThat(applicationContext).isNotNull();
        
        // 验证关键Bean存在
        assertThat(applicationContext.containsBean("userController")).isTrue();
    }

    @Test
    void testRpcConfigurationLoaded() {
        // 验证RPC配置正确加载
        assertThat(applicationContext.getEnvironment().getProperty("rpc.name")).isEqualTo("test-consumer");
    }

    @Test
    void testWebServerStarted() {
        // 验证Web服务器正常启动
        assertThat(port).isGreaterThan(0);
        
        // 测试根路径可访问性
        String url = "http://localhost:" + port + "/user/health";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
