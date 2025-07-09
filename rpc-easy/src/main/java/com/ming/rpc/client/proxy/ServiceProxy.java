package com.ming.rpc.client.proxy;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import com.ming.rpc.model.request.RpcRequest;
import com.ming.rpc.model.response.RpcResponse;
import com.ming.rpc.protocol.serialize.JdkSerializer;
import com.ming.rpc.protocol.serialize.Serializer;

/**
 * 服务代理（JDK动态代理）
 */
public class ServiceProxy implements InvocationHandler{
    /**
     * 调用代理
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke (Object proxy, Method method, Object[] args) throws Throwable{
        // 如果是Object类的方法，直接调用
        if (method.getDeclaringClass() == Object.class) {
            return method.invoke(this, args);
        }
        
        //指定序列化器
        Serializer serializer = new JdkSerializer();
        
        // 获取服务名称 - 使用接口全限定名
        String serviceName = method.getDeclaringClass().getName();
        System.out.println("客户端准备调用服务: " + serviceName);
        System.out.println("调用方法: " + method.getName());
        
        //创建请求对象
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(serviceName)
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .args(args)
                .build();
        
        System.out.println("构建RPC请求: " + rpcRequest);
        
        try{
            //序列化
            byte[] bodyBytes = serializer.serialize(rpcRequest);
            System.out.println("请求序列化完成，准备发送");
            
            //发送请求
            //这里地址硬编码了，后续需要使用注册中心和服务发现机制解决
            // 修改端口为8081
            String url = "http://localhost:8081";
            System.out.println("发送请求到: " + url);
            
            try(
                HttpResponse httpResponse = HttpRequest.post(url)
                .body(bodyBytes)
                .execute()){
                    System.out.println("收到响应，状态码: " + httpResponse.getStatus());
                    
                    byte[] result = httpResponse.bodyBytes();
                    System.out.println("响应体大小: " + result.length + "字节");
                    
                    //反序列化
                    RpcResponse rpcResponse = serializer.deserialize(result, RpcResponse.class);
                    
                    // 检查响应是否有异常
                    if (rpcResponse.getException() != null) {
                        System.err.println("RPC调用异常: " + rpcResponse.getMessage());
                        throw rpcResponse.getException();
                    }
                    
                    System.out.println("RPC调用成功，响应消息: " + rpcResponse.getMessage());
                    return rpcResponse.getData();
                }
        }catch(IOException e){
            System.err.println("RPC调用IO异常: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("RPC调用失败", e);
        }
    }
}

