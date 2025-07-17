package com.ming.example.consumer;

import com.ming.rpc.springboot.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot RPC 服务消费者启动类
 * 
 * 使用@EnableRpc注解启用RPC功能，needServer=false表示不启动RPC服务器
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
@EnableRpc(needServer = false)
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
