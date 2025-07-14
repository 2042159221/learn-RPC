package com.ming.rpc.serializer;

import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
import com.ming.rpc.model.ServiceMetaInfo;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;

public class JsonSerializerTest {

    @Test
    public void testJsonSerializer() throws IOException {
        JsonSerializer serializer = new JsonSerializer();

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
        Assert.assertArrayEquals(request.getParameterTypes(), deserializedRequest.getParameterTypes());
        Assert.assertArrayEquals(request.getArgs(), deserializedRequest.getArgs());

        // 测试响应序列化和反序列化
        RpcResponse response = new RpcResponse();
        response.setMessage("success");
        response.setException(null);
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("testService");
        serviceMetaInfo.setServiceVersion("1.0");
        response.setData(serviceMetaInfo);
        response.setDataType(ServiceMetaInfo.class);


        byte[] responseBytes = serializer.serialize(response);
        Assert.assertNotNull(responseBytes);

        RpcResponse deserializedResponse = serializer.deserialize(responseBytes, RpcResponse.class);
        Assert.assertNotNull(deserializedResponse);
        Assert.assertEquals(response.getMessage(), deserializedResponse.getMessage());
        Assert.assertEquals(response.getException(), deserializedResponse.getException());
        Assert.assertEquals(response.getData(), deserializedResponse.getData());
        Assert.assertEquals(response.getDataType(), deserializedResponse.getDataType());

        // 测试 null
        byte[] nullBytes = serializer.serialize(null);
        Object nullObj = serializer.deserialize(nullBytes, Object.class);
        Assert.assertNull(nullObj);
    }
} 