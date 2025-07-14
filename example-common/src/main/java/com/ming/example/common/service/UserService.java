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

    /**
     * 用于测试 mock 接口返回值
     * @return 
     */
    default short getNumber() {
        return 1;
    }
}
