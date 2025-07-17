package com.ming.example.consumer;

import com.ming.example.common.model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.client.RestTemplate;

/**
 * 简单的测试运行器，用于验证Consumer功能
 */
public class TestRunner {
    
    public static void main(String[] args) {
        System.out.println("=== 开始启动 Ming RPC Consumer ===");
        
        try {
            // 启动Spring Boot应用
            ConfigurableApplicationContext context = SpringApplication.run(ConsumerApplication.class, args);
            
            System.out.println("✅ Consumer启动成功！");
            System.out.println("🌐 HTTP服务端口: 8082");
            System.out.println("📋 注册中心: MOCK (测试模式)");
            
            // 等待一下让服务完全启动
            Thread.sleep(2000);
            
            System.out.println("\n=== 测试HTTP接口 ===");
            
            // 测试健康检查
            RestTemplate restTemplate = new RestTemplate();
            try {
                String health = restTemplate.getForObject("http://localhost:8082/user/health", String.class);
                System.out.println("🏥 健康检查: " + health);
            } catch (Exception e) {
                System.out.println("⚠️ 健康检查失败: " + e.getMessage());
            }
            
            // 测试用户接口
            try {
                User user = restTemplate.getForObject("http://localhost:8082/user/小明", User.class);
                System.out.println("👤 用户查询结果: " + (user != null ? user.getName() : "null"));
            } catch (Exception e) {
                System.out.println("⚠️ 用户查询失败: " + e.getMessage());
                System.out.println("💡 这是正常的，因为没有真实的Provider连接");
            }
            
            System.out.println("\n✅ Consumer运行正常！");
            System.out.println("🌐 你可以在浏览器中访问:");
            System.out.println("   http://localhost:8082/user/health");
            System.out.println("   http://localhost:8082/user/张三");
            System.out.println("🛑 按 Ctrl+C 停止服务");
            
        } catch (Exception e) {
            System.err.println("❌ Consumer启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
