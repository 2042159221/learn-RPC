package com.ming.example.provider.service;

import com.ming.example.common.model.User;
import com.ming.example.common.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UserServiceImpl单元测试
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
class UserServiceImplTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl();
    }

    @Test
    void testGetUser() {
        // 准备测试数据
        User inputUser = new User();
        inputUser.setName("test-user");

        // 执行测试
        User result = userService.getUser(inputUser);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Provider processed: test-user");
    }

    @Test
    void testGetUserWithNullName() {
        // 准备测试数据
        User inputUser = new User();
        inputUser.setName(null);

        // 执行测试
        User result = userService.getUser(inputUser);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Provider processed: null");
    }

    @Test
    void testGetUserWithEmptyName() {
        // 准备测试数据
        User inputUser = new User();
        inputUser.setName("");

        // 执行测试
        User result = userService.getUser(inputUser);

        // 验证结果
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Provider processed: ");
    }
}
