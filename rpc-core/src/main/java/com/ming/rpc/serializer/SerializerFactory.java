package com.ming.rpc.serializer;

import com.ming.rpc.spi.SpiLoader;

/**
 * 序列化器工厂（工厂模式，用于获取序列化器）
 */
public class SerializerFactory {
    static {
        SpiLoader.load(Serializer.class);
    }

    /**
     * 默认序列化器
     */
    private static final Serializer DEFAULT_SERIALIZER = new JdkSerializer();

    /**
     * 获取实例
     * @param key 序列化器类型
     * @return 序列化器
     */
    public static Serializer getInstance(String key) {
        return SpiLoader.getInstance(Serializer.class, key);
    }

    /** */
    public static Serializer getInstance() {
        return DEFAULT_SERIALIZER;
    }
}
