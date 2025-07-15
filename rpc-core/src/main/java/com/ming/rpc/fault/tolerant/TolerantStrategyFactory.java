package com.ming.rpc.fault.tolerant;

import com.ming.rpc.spi.SpiLoader;

/**
 * 容错策略工厂（工厂模式，用于获取容错策略对象）
 */
public class TolerantStrategyFactory {

    static {
        SpiLoader.load(TolerantStrategy.class);
    }

    /**
     * 默认容错策略
     */
 
    private static final TolerantStrategy DEFAULT_TOLERANT_STRATEGY = new FailFastTolerantStrategy();

    /**
     * 获取实例
     * @param key 键
     * @return 实例
     */
    public static TolerantStrategy getInstance(String key) {
        TolerantStrategy tolerantStrategy = SpiLoader.getInstance(TolerantStrategy.class, key);
        return tolerantStrategy == null ? DEFAULT_TOLERANT_STRATEGY : tolerantStrategy;
    }
}
