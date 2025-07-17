package com.ming.example.consumer.controller;

import com.ming.example.common.model.User;
import com.ming.example.common.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * UserController单元测试
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUser() {
        // 准备测试数据
        String userName = "test-user";
        User mockResult = new User();
        mockResult.setName("Provider processed: " + userName);

        // 模拟远程服务调用
        when(userService.getUser(any(User.class))).thenReturn(mockResult);

        // 执行测试
        User result = userController.getUser(userName);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Provider processed: test-user");
    }

    @Test
    void testCreateUser() {
        // 准备测试数据
        User inputUser = new User();
        inputUser.setName("create-user");

        User mockResult = new User();
        mockResult.setName("Provider processed: create-user");

        // 模拟远程服务调用
        when(userService.getUser(any(User.class))).thenReturn(mockResult);

        // 执行测试
        User result = userController.createUser(inputUser);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Provider processed: create-user");
    }

    @Test
    void testHealth() {
        // 执行测试
        String result = userController.health();

        // 验证结果
        assertThat(result).isEqualTo("Consumer is healthy");
    }

    @Test
    void testGetUserWithEmptyName() {
        // 准备测试数据
        String userName = "";
        User mockResult = new User();
        mockResult.setName("Provider processed: ");

        // 模拟远程服务调用
        when(userService.getUser(any(User.class))).thenReturn(mockResult);

        // 执行测试
        User result = userController.getUser(userName);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Provider processed: ");
    }
}
