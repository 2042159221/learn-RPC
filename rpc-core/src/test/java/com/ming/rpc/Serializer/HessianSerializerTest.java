package com.ming.rpc.serializer;

import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
import com.ming.rpc.model.ServiceMetaInfo;
import com.ming.rpc.spi.SpiLoader;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Hessian 序列化器测试
 */
public class HessianSerializerTest {

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

    @Test
    public void testHessianSerializer() throws IOException {
        HessianSerializer serializer = new HessianSerializer();

        // 测试简单对象的序列化和反序列化
        TestUser user = new TestUser("ming", 22);
        byte[] userBytes = serializer.serialize(user);
        Assert.assertNotNull(userBytes);

        TestUser deserializedUser = serializer.deserialize(userBytes, TestUser.class);
        Assert.assertNotNull(deserializedUser);
        Assert.assertEquals(user.name, deserializedUser.name);
        Assert.assertEquals(user.age, deserializedUser.age);

        // 测试请求序列化和反序列化
        RpcRequest request = new RpcRequest();
        request.setServiceName("testService");
        request.setMethodName("testMethod");
        request.setServiceVersion("1.0");
        request.setParameterTypes(new Class[]{String.class, int.class});
        request.setArgs(new Object[]{"hello", 123});

        byte[] requestBytes = serializer.serialize(request);
        Assert.assertNotNull(requestBytes);

        RpcRequest deserializedRequest = serializer.deserialize(requestBytes, RpcRequest.class);
        Assert.assertNotNull(deserializedRequest);
        Assert.assertEquals(request.getServiceName(), deserializedRequest.getServiceName());
        Assert.assertEquals(request.getMethodName(), deserializedRequest.getMethodName());
        Assert.assertEquals(request.getServiceVersion(), deserializedRequest.getServiceVersion());
        Assert.assertEquals(request.getParameterTypes()[0], deserializedRequest.getParameterTypes()[0]);
        Assert.assertEquals(request.getParameterTypes()[1], deserializedRequest.getParameterTypes()[1]);
        Assert.assertEquals(request.getArgs()[0], deserializedRequest.getArgs()[0]);
        Assert.assertEquals(request.getArgs()[1], deserializedRequest.getArgs()[1]);

        // 测试响应序列化和反序列化
        RpcResponse response = new RpcResponse();
        response.setMessage("success");
        response.setException(null);
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("testService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(8080);
        response.setData(serviceMetaInfo);
        response.setDataType(ServiceMetaInfo.class);

        byte[] responseBytes = serializer.serialize(response);
        Assert.assertNotNull(responseBytes);

        RpcResponse deserializedResponse = serializer.deserialize(responseBytes, RpcResponse.class);
        Assert.assertNotNull(deserializedResponse);
        Assert.assertEquals(response.getMessage(), deserializedResponse.getMessage());
        Assert.assertNull(deserializedResponse.getException());
        
        ServiceMetaInfo deserializedMetaInfo = (ServiceMetaInfo) deserializedResponse.getData();
        Assert.assertNotNull(deserializedMetaInfo);
        Assert.assertEquals(serviceMetaInfo.getServiceName(), deserializedMetaInfo.getServiceName());
        Assert.assertEquals(serviceMetaInfo.getServiceVersion(), deserializedMetaInfo.getServiceVersion());
        Assert.assertEquals(serviceMetaInfo.getServiceHost(), deserializedMetaInfo.getServiceHost());
        Assert.assertEquals(serviceMetaInfo.getServicePort(), deserializedMetaInfo.getServicePort());
        Assert.assertEquals(response.getDataType(), deserializedResponse.getDataType());

        // 测试 null
        byte[] nullBytes = serializer.serialize(null);
        Object nullObj = serializer.deserialize(nullBytes, Object.class);
        Assert.assertNull(nullObj);
    }

    /**
     * 测试 HessianSerializer 实例化
     */
    @Test
    public void testHessianSerializerInstance() {
        // 直接使用HessianSerializer，不通过SPI加载
        Serializer serializer = new HessianSerializer();
        Assert.assertNotNull(serializer);
        Assert.assertTrue(serializer instanceof HessianSerializer);
    }
    
    /**
     * 测试 SerializerFactory 是否能正确获取 HessianSerializer
     */
    @Test
    public void testSerializerFactory() {
        // 先加载SPI配置
        Map<String, Class<?>> keyClassMap = SpiLoader.load(Serializer.class);
        System.out.println("加载的SPI配置：" + keyClassMap);
        
        // 通过SerializerFactory获取HessianSerializer
        Serializer serializer = SerializerFactory.getInstance(SerializerKeys.HESSIAN);
        Assert.assertNotNull(serializer);
        Assert.assertTrue(serializer instanceof HessianSerializer);
    }
} 