package com.ming.rpc.loadbalancer;

/**
 * 负载均衡器键名常量
 */
public interface LoadBalancerKeys {
    /**
     * 轮询
     */
    String ROUND_ROBIN = "roundRobin";//轮询策略
    String RANDOM = "random";//随机策略 
    String CONSISTENT_HASH = "consistentHash";//一致性哈希策略
}
