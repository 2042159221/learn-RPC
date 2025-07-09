package com.ming.example.common.service;

import com.ming.example.common.modle.User;


/**
 * 用户服务
 */

public interface UserService {
    /**
     * 获取用户
     * @return 用户
     */
    User getUser(User user);
}
