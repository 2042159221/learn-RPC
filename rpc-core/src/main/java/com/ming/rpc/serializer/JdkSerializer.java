package com.ming.rpc.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * JDK 序列化器
 */
public class JdkSerializer implements Serializer {

    /**
     * 序列化
     * @param object 对象
     * @param <T> 泛型
     * @return 字节数组
     * @throws IOException 
     */
    @Override
    public <T> byte[] serialize(T object) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
        return outputStream.toByteArray();
    }   

    /**
     * 反序列化
     * @param bytes 字节数组
     * @param type
     * @param <T> 泛型
     * @return 对象
     * @throws IOException 
     */
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> tClass) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        try {
            Object obj = objectInputStream.readObject();
            return tClass.cast(obj);
        } catch (ClassNotFoundException e) {
            throw new IOException("Class not found", e);
        } finally {
            objectInputStream.close();
        }
    }
}
