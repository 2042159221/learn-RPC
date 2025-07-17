package com.ming.rpc.springboot.annotation;

import com.ming.rpc.fault.retry.RetryStrategyKeys;
import com.ming.rpc.fault.tolerant.TolerantStrategyKeys;
import com.ming.rpc.loadbalancer.LoadBalancerKeys;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC服务消费者注解
 * 
 * 用于标记需要注入RPC服务代理的字段。
 * 框架会自动为标记的字段创建并注入相应的RPC服务代理对象。
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RpcReference {

    /**
     * 服务接口类
     * 如果不指定，将使用字段的类型作为服务接口
     * 
     * @return 服务接口类
     */
    Class<?> interfaceClass() default void.class;

    /**
     * 服务版本号
     * 用于指定要调用的服务版本
     * 
     * @return 服务版本号
     */
    String version() default "";

    /**
     * 服务分组
     * 用于指定要调用的服务分组
     * 
     * @return 服务分组名称
     */
    String group() default "";

    /**
     * 负载均衡策略
     * 
     * @return 负载均衡策略名称
     */
    String loadBalancer() default LoadBalancerKeys.ROUND_ROBIN;

    /**
     * 重试策略
     * 
     * @return 重试策略名称
     */
    String retryStrategy() default RetryStrategyKeys.NO;

    /**
     * 容错策略
     * 
     * @return 容错策略名称
     */
    String tolerantStrategy() default TolerantStrategyKeys.FAIL_FAST;

    /**
     * 是否启用模拟调用
     * 启用后将使用Mock服务而不是真实的远程调用
     * 
     * @return true表示启用模拟调用
     */
    boolean mock() default false;

    /**
     * 调用超时时间（毫秒）
     * 
     * @return 超时时间
     */
    long timeout() default 3000L;
}
