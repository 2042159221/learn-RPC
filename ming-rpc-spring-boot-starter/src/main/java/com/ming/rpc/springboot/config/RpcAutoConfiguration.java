package com.ming.rpc.springboot.config;

import com.ming.rpc.RpcApplication;
import com.ming.rpc.config.RpcConfig;
import com.ming.rpc.springboot.processor.RpcBeanPostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Ming RPC 自动配置类
 * 
 * 负责自动配置RPC框架的核心组件
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(RpcConfigurationProperties.class)
@ConditionalOnProperty(prefix = "rpc", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RpcAutoConfiguration {

    /**
     * 创建RpcConfig Bean
     * 
     * @param properties 配置属性
     * @return RpcConfig实例
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(RpcConfig.class)
    public RpcConfig rpcConfig(RpcConfigurationProperties properties) {
        log.info("Creating RpcConfig bean with properties: {}", properties);
        RpcConfig rpcConfig = properties.toRpcConfig();
        
        // 初始化RPC应用
        try {
            RpcApplication.init(rpcConfig);
            log.info("Ming RPC framework initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Ming RPC framework", e);
            throw new RuntimeException("Ming RPC initialization failed", e);
        }
        
        return rpcConfig;
    }

    /**
     * 创建RPC Bean后处理器
     * 
     * @return RpcBeanPostProcessor实例
     */
    @Bean
    @ConditionalOnMissingBean(RpcBeanPostProcessor.class)
    public RpcBeanPostProcessor rpcBeanPostProcessor() {
        log.info("Creating RpcBeanPostProcessor bean");
        return new RpcBeanPostProcessor();
    }
}
