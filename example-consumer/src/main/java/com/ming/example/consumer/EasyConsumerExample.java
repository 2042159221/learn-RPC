package com.ming.example.consumer;

import com.ming.example.common.modle.User;
import com.ming.example.common.service.UserService;
import com.ming.rpc.client.proxy.ServiceProxyFactory;

/**
 * 服务消费者示例
 * 演示如何使用RPC框架调用远程服务
 */
public class EasyConsumerExample {

    public static void main(String[] args) {
        try {
            System.out.println("======== 服务消费者启动 ========");
            
            // 显示服务接口信息
            System.out.println("需要调用的服务接口: " + UserService.class.getName());
            
            // 动态代理
            System.out.println("创建服务代理...");
            UserService userService = ServiceProxyFactory.getProxy(UserService.class);
            System.out.println("服务代理创建成功，代理类: " + userService.getClass().getName());
            
            // 创建用户对象
            System.out.println("准备创建用户对象...");
            User user = new User();
            user.setName("张三");
            System.out.println("用户对象创建成功: name=" + user.getName());
            
            // 远程调用
            System.out.println("======== 开始RPC调用 ========");
            System.out.println("调用方法: getUser");
            
            try {
                User result = userService.getUser(user);
                System.out.println("RPC调用成功!");
                
                if (result != null) {
                    System.out.println("返回结果：" + result.getName());
                } else {
                    System.out.println("返回结果为null");
                }
            } catch (Exception e) {
                System.err.println("RPC调用失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("======== 调用完成 ========");
        } catch (Exception e) {
            System.err.println("程序执行异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
