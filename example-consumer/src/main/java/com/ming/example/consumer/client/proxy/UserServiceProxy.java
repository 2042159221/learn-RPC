package com.ming.example.consumer.client.proxy;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

import com.ming.example.common.model.User;
import com.ming.example.common.service.UserService;
import com.ming.rpc.serializer.JdkSerializer;
import com.ming.rpc.serializer.Serializer;
import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
/**
 * static proxy                                     
 */
public class UserServiceProxy implements UserService {
    public User getUser(User user) {
        System.out.println("开始调用远程服务...");
        //reference the user service
        Serializer Serializer = new JdkSerializer();
        //send the request to the server
        RpcRequest rpcRequest = RpcRequest.builder()
                .serviceName(UserService.class.getName())
                .methodName("getUser")
                .parameterTypes(new Class[]{User.class})
                .args(new Object[]{user})
                .build();
        try{
            System.out.println("正在序列化请求...");
            byte[] bodyBytes = Serializer.serialize(rpcRequest);
            byte[] result;
            
            System.out.println("正在发送HTTP请求到localhost:8080...");
            try(HttpResponse httpResponse = HttpRequest.post("http://localhost:8080")
            .body(bodyBytes)
            .execute()){
                System.out.println("请求已发送，正在获取响应...");
                result = httpResponse.bodyBytes();
            }
            
            System.out.println("正在反序列化响应...");
            RpcResponse rpcResponse = Serializer.deserialize(result, RpcResponse.class);
            System.out.println("响应反序列化成功，正在提取结果...");
            return (User) rpcResponse.getData();
        }catch(Exception e){
            System.err.println("RPC调用失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);  
        }
    }
}
