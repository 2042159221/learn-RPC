package com.ming.rpc.serializer;

import java.io.IOException;

/**
 * 序列化接口
 */
public interface Serializer {
    /**
     * 序列化
     * @param object 对象
     * @param <T> 泛型
     * @return 字节数组
     * @throws IOException 
     */
    <T> byte[] serialize(T object) throws IOException;

    /**
     * 反序列化
     * @param bytes 字节数组
     * @param tClass 类
     * @param <T> 泛型
     * @return 对象
     */
    <T> T deserialize(byte[] bytes, Class<T> tClass) throws IOException;
}
