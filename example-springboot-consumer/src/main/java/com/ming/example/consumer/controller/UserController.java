package com.ming.example.consumer.controller;

import com.ming.example.common.model.User;
import com.ming.example.common.service.UserService;
import com.ming.rpc.springboot.annotation.RpcReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 
 * 使用@RpcReference注解注入远程RPC服务
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 注入远程用户服务
     * 使用@RpcReference注解自动创建代理对象
     */
    @RpcReference
    private UserService userService;

    /**
     * 获取用户信息
     * 
     * @param name 用户名
     * @return 用户信息
     */
    @GetMapping("/{name}")
    public User getUser(@PathVariable("name") String name) {
        log.info("Consumer received request for user: {}", name);

        // 创建用户对象
        User user = new User();
        user.setName(name);

        // 检查RPC服务是否可用
        if (userService == null) {
            log.warn("RPC service is not available, returning mock response");
            User mockResult = new User();
            mockResult.setName("Mock response (RPC service unavailable): " + name);
            return mockResult;
        }

        try {
            // 调用远程服务
            User result = userService.getUser(user);
            log.info("Consumer received response: {}", result);
            return result;
        } catch (Exception e) {
            log.error("RPC call failed: {}", e.getMessage());
            User errorResult = new User();
            errorResult.setName("Error response (RPC call failed): " + name);
            return errorResult;
        }
    }

    /**
     * 创建用户
     * 
     * @param user 用户信息
     * @return 处理结果
     */
    @PostMapping
    public User createUser(@RequestBody User user) {
        log.info("Consumer received create user request: {}", user);
        
        // 调用远程服务
        User result = userService.getUser(user);
        
        log.info("Consumer received create user response: {}", result);
        return result;
    }

    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping("/health")
    public String health() {
        return "Consumer is healthy";
    }
}
