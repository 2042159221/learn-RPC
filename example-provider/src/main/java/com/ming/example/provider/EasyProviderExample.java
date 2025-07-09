package com.ming.example.provider;

import com.ming.example.common.service.UserService;
import com.ming.example.provider.service.impl.UserServiceImpl;
import com.ming.rpc.registry.LocalRegistry;
import com.ming.rpc.server.HttpServer;
import com.ming.rpc.server.VertexHttpServer;

/**
 * 服务提供者启动类
 */
public class EasyProviderExample {

    public static void main(String[] args) {
        try {
            System.out.println("======== 服务提供者开始启动 ========");
            
            // 打印即将注册的服务信息
            String serviceName = UserService.class.getName();
            Class<?> implClass = UserServiceImpl.class;
            
            System.out.println("准备注册服务: " + serviceName);
            System.out.println("服务接口: " + serviceName);
            System.out.println("服务实现类: " + implClass.getName());
            
            // register service
            LocalRegistry.register(serviceName, implClass);
            System.out.println("服务注册成功: " + serviceName + " -> " + implClass.getName());
            
            // 验证服务是否注册成功
            Class<?> registeredClass = LocalRegistry.get(serviceName);
            if (registeredClass != null) {
                System.out.println("验证服务注册: 成功找到服务 " + serviceName);
                System.out.println("已注册服务列表: " + LocalRegistry.listServices());
            } else {
                System.err.println("警告: 服务注册后无法找到: " + serviceName);
            }
            
            // 这里可以添加RPC服务暴露的代码
            // 暂时仅创建基础结构，后续会实现RPC服务发布功能
            System.out.println("正在创建HTTP服务器...");
            HttpServer httpServer = new VertexHttpServer();
            // 修改端口号为8081，避免端口冲突
            int port = 8081;
            System.out.println("正在启动HTTP服务器，监听端口: " + port + "...");
            httpServer.doStart(port);
            System.out.println("======== 服务器启动成功 ========");
            System.out.println("服务地址: http://localhost:" + port);
            
            // 保持服务运行
            try {
                System.out.println("按Ctrl+C停止服务...");
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                System.err.println("服务被中断: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("服务启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 