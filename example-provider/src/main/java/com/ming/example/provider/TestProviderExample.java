package com.ming.example.provider;

import com.ming.example.common.service.UserService;
import com.ming.example.provider.service.impl.UserServiceImpl;
import com.ming.rpc.RpcApplication;
import com.ming.rpc.config.RpcConfig;
import com.ming.rpc.registry.LocalRegistry;
import com.ming.rpc.server.tcp.VertexTcpServer;

/**
 * 测试服务提供者启动类 - 不依赖外部注册中心
 */
public class TestProviderExample {

    public static void main(String[] args) {
        try {
            System.out.println("======== 测试服务提供者开始启动 ========");
            
            // 创建自定义配置，避免连接外部注册中心
            RpcConfig rpcConfig = new RpcConfig();
            rpcConfig.setName("ming-rpc-test");
            rpcConfig.setVersion("1.0");
            rpcConfig.setServerHost("localhost");
            rpcConfig.setServerPort(8080);
            rpcConfig.setMock(false);
            
            // 使用自定义配置初始化RPC框架
            RpcApplication.init(rpcConfig);
            System.out.println("RPC框架初始化完成");
            
            // 注册服务到本地注册中心
            String serviceName = UserService.class.getName();
            Class<?> implClass = UserServiceImpl.class;
            
            System.out.println("准备注册服务: " + serviceName);
            System.out.println("服务实现类: " + implClass.getName());
            
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
            
            // 启动TCP服务器
            System.out.println("正在启动TCP服务器...");
            VertexTcpServer tcpServer = new VertexTcpServer();
            int port = rpcConfig.getServerPort();
            System.out.println("正在启动TCP服务器，监听端口: " + port + "...");
            tcpServer.doStart(port);
            System.out.println("======== TCP服务器启动成功 ========");
            System.out.println("服务地址: tcp://localhost:" + port);
            
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
