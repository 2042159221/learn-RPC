package com.ming.rpc.serializer;

import com.ming.rpc.model.RpcRequest;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

/**
 * 序列化器测试
 */
public class SerializerTest {

    /**
     * 用于测试的内部类
     */
    static class TestUser implements Serializable {
        private String name;
        private int age;

        public TestUser(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestUser testUser = (TestUser) o;
            return age == testUser.age && Objects.equals(name, testUser.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }
    }

    /**
     * 测试 JdkSerializer
     * @throws IOException
     */
    @Test
    public void testJdkSerializer() throws IOException {
        Serializer serializer = new JdkSerializer();
        TestUser user = new TestUser("ming", 22);

        // 序列化
        byte[] bytes = serializer.serialize(user);
        Assert.assertNotNull(bytes);

        // 反序列化
        TestUser deserializedUser = serializer.deserialize(bytes, TestUser.class);
        Assert.assertNotNull(deserializedUser);
        Assert.assertEquals(user, deserializedUser);
        
        // 测试 null
        byte[] nullBytes = serializer.serialize(null);
        Object nullObj = serializer.deserialize(nullBytes, Object.class);
        Assert.assertNull(nullObj);
    }

    /**
     * 测试 SerializerFactory
     */
    @Test
    public void testSerializerFactory() {
        Serializer serializer = SerializerFactory.getInstance("jdk");
        Assert.assertNotNull(serializer);
        Assert.assertTrue(serializer instanceof JdkSerializer);

        // 测试SPI加载
        Serializer spiSerializer = SerializerFactory.getInstance();
        Assert.assertNotNull(spiSerializer);
    }
}
