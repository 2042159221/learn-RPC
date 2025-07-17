package com.ming.rpc.springboot.annotation;

import com.ming.rpc.springboot.bootstrap.RpcConsumerBootstrap;
import com.ming.rpc.springboot.bootstrap.RpcInitBootstrap;
import com.ming.rpc.springboot.bootstrap.RpcProviderBootstrap;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 启用 Ming RPC 注解
 * 
 * 该注解用于在Spring Boot应用中启用Ming RPC框架功能。
 * 通过导入相关的Bootstrap类来自动配置RPC组件。
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({RpcInitBootstrap.class, RpcProviderBootstrap.class, RpcConsumerBootstrap.class})
public @interface EnableRpc {

    /**
     * 是否需要启动RPC服务器
     * 
     * @return true表示启动服务器（适用于服务提供者），false表示不启动（适用于纯消费者）
     */
    boolean needServer() default true;

    /**
     * 扫描的基础包路径
     * 如果未指定，将扫描使用该注解的类所在的包及其子包
     * 
     * @return 要扫描的包路径数组
     */
    String[] basePackages() default {};

    /**
     * 扫描的基础包类
     * 将使用指定类所在的包作为扫描基础包
     * 
     * @return 基础包类数组
     */
    Class<?>[] basePackageClasses() default {};
}
