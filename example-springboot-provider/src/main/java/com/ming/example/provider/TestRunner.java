package com.ming.example.provider;

import com.ming.example.common.model.User;
import com.ming.example.provider.service.UserServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 简单的测试运行器，用于验证Provider功能
 */
public class TestRunner {
    
    public static void main(String[] args) {
        System.out.println("=== 开始启动 Ming RPC Provider ===");
        
        try {
            // 启动Spring Boot应用
            ConfigurableApplicationContext context = SpringApplication.run(ProviderApplication.class, args);
            
            System.out.println("✅ Provider启动成功！");
            System.out.println("🌐 HTTP服务端口: 8081");
            System.out.println("🔌 RPC服务端口: 8080");
            System.out.println("📋 注册中心: MOCK (测试模式)");
            
            // 测试服务功能
            UserServiceImpl userService = context.getBean(UserServiceImpl.class);
            
            System.out.println("\n=== 测试服务功能 ===");
            User testUser = new User();
            testUser.setName("测试用户");
            
            User result = userService.getUser(testUser);
            System.out.println("📤 发送请求: " + testUser.getName());
            System.out.println("📥 收到响应: " + result.getName());
            
            System.out.println("\n✅ Provider运行正常，等待Consumer连接...");
            System.out.println("💡 提示: 现在可以启动Consumer来测试RPC调用");
            System.out.println("🛑 按 Ctrl+C 停止服务");
            
        } catch (Exception e) {
            System.err.println("❌ Provider启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
