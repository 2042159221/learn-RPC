package com.ming.rpc.test;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

/**
 * Lombok功能测试类
 * 用于验证Lombok注解是否正常工作
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class LombokTest {
    
    private String name;
    private int age;
    private String email;
    
    /**
     * 测试方法
     */
    public void testLombok() {
        log.info("开始测试Lombok功能...");
        log.info("姓名: {}, 年龄: {}, 邮箱: {}", name, age, email);
        log.info("Lombok测试完成！");
    }
    
    /**
     * 主方法用于测试
     */
    public static void main(String[] args) {
        // 测试Builder模式
        LombokTest test1 = LombokTest.builder()
                .name("张三")
                .age(25)
                .email("zhangsan@example.com")
                .build();
        
        test1.testLombok();
        
        // 测试无参构造器和setter方法
        LombokTest test2 = new LombokTest();
        test2.setName("李四");
        test2.setAge(30);
        test2.setEmail("lisi@example.com");
        
        test2.testLombok();
        
        // 测试getter方法
        System.out.println("通过getter获取信息:");
        System.out.println("姓名: " + test2.getName());
        System.out.println("年龄: " + test2.getAge());
        System.out.println("邮箱: " + test2.getEmail());
        
        // 测试全参构造器
        LombokTest test3 = new LombokTest("王五", 28, "wangwu@example.com");
        test3.testLombok();
        
        System.out.println("✅ Lombok所有功能测试通过！");
    }
}
