package com.ming.example.consumer;

import com.ming.rpc.core.RpcConsumer;
import com.ming.example.common.service.UserService;

/**
 * 服务消费者示例
 * 
 */
public class ConsumerExample {
    public static void main(String[] args) {
        // 创建一个消费者
        RpcConsumer consumer = new RpcConsumer();
        // 设置服务提供者的地址
        consumer.setProviderAddress("127.0.0.1:8080");
        // 设置服务提供者的接口
        consumer.setServiceInterface(UserService.class);
    }
}
