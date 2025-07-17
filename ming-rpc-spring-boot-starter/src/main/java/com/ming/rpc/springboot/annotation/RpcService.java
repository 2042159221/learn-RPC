package com.ming.rpc.springboot.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC服务提供者注解
 * 
 * 用于标记RPC服务实现类，被标记的类将自动注册到RPC框架中。
 * 该注解同时具有Spring的@Component功能，会被注册为Spring Bean。
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface RpcService {

    /**
     * 服务接口类
     * 如果不指定，将自动使用实现类的第一个接口
     * 
     * @return 服务接口类
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务版本号
     * 用于区分同一接口的不同版本实现
     * 
     * @return 服务版本号
     */
    String version() default "";

    /**
     * 服务分组
     * 用于对服务进行分组管理
     * 
     * @return 服务分组名称
     */
    String group() default "";

    /**
     * 服务标签
     * 用于服务的额外标识和路由
     * 
     * @return 服务标签数组
     */
    String[] tags() default {};
}
