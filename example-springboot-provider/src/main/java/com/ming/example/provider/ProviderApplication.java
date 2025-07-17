package com.ming.example.provider;

import com.ming.rpc.springboot.annotation.EnableRpc;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot RPC 服务提供者启动类
 * 
 * 使用@EnableRpc注解启用RPC功能，needServer=true表示启动RPC服务器
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@SpringBootApplication
@EnableRpc(needServer = true)
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
