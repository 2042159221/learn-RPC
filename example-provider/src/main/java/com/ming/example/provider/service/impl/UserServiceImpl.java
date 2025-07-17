package com.ming.example.provider.service.impl;

import com.ming.example.common.model.User;
import com.ming.example.common.service.UserService;

/**
 * 用户服务实现类
 */
public class UserServiceImpl implements UserService {
    
    /**
     * 获取用户
     * 打印用户名称，并返回用户对象
     * @param user
     * @return 用户
     */
    @Override
    public User getUser(User user) {        
        
        
        // 打印用户名称
        System.out.println("用户名称: " + user.getName());
        
        // 返回用户对象
        return user;
    }
} 