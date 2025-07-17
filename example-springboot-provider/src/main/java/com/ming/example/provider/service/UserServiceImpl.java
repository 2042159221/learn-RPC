package com.ming.example.provider.service;

import com.ming.example.common.model.User;
import com.ming.example.common.service.UserService;
import com.ming.rpc.springboot.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 * 
 * 使用@RpcService注解标记为RPC服务提供者
 * 同时使用@Service注解注册为Spring Bean
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Service
@RpcService
public class UserServiceImpl implements UserService {

    @Override
    public User getUser(User user) {
        log.info("Provider received request for user: {}", user);
        
        // 模拟业务逻辑
        User result = new User();
        result.setName("Provider processed: " + user.getName());
        
        log.info("Provider returning user: {}", result);
        return result;
    }
}
