package com.ming.rpc.serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Kryo 序列化器
 */
public class KryoSerializer implements Serializer {

    /**
     * kryo 线程不安全，使用ThreadLocal 保证线程只有一个 Kryo
     */
    private static final ThreadLocal<Kryo> KRYO_THREAD_LOCAL = ThreadLocal.withInitial(() -> {
        Kryo kryo = new Kryo();
        // 设置动态序列化和反序列化类，不提前注册所有类（可能有安全问题）
        kryo.setRegistrationRequired(false);
        return kryo;
    });

    @Override
    public byte[] serialize(Object obj) throws IOException {
        if (obj == null) {
            return new byte[0];
        }
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Output output = new Output(outputStream);
        kryo.writeObject(output, obj);
        output.close();
        return outputStream.toByteArray();
    }

    @Override 
    public <T> T deserialize(byte[] bytes, Class<T> classType) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        Kryo kryo = KRYO_THREAD_LOCAL.get();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        Input input = new Input(inputStream);
        T result  = kryo.readObject(input, classType);
        input.close();
        return result;
    }

}
