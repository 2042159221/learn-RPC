package com.ming.rpc.loadbalancer;

import com.ming.rpc.spi.SpiLoader;

/**
 * 负载均衡器工厂（工厂模式，用于获取负载均衡器对象）
 */
public class LoadBalancerFactory {
    static {
        SpiLoader.load(LoadBalancer.class);
    }

    /**
     * 默认负载均衡器
     */
    private static final LoadBalancer DEFAULT_LOAD_BALANCER = new RoundRobinLoadBalancer();

    /**
     * 获取实列
     * @param key 负载均衡器类型
     * @return 负载均衡器
     */
    public static LoadBalancer getInstance(String key) {
        return SpiLoader.getInstance(LoadBalancer.class, key);
    }

}
