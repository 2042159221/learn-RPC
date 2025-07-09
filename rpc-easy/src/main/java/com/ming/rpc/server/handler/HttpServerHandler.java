package com.ming.rpc.server.handler;

import com.ming.rpc.model.request.RpcRequest;
import com.ming.rpc.model.response.RpcResponse;
import com.ming.rpc.protocol.serialize.JdkSerializer;
import com.ming.rpc.protocol.serialize.Serializer;
import com.ming.rpc.registry.LocalRegistry;

import java.lang.reflect.Method;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.buffer.Buffer;

/**
 * Http request handler
 */

public class HttpServerHandler implements Handler<HttpServerRequest>{

    @Override
    public void handle(HttpServerRequest request) {
        // reference the serializer
        final Serializer serializer = new JdkSerializer();
        // record the log
        System.out.println("receive request " + request.method() + " " + request.uri());

        //Async read the request body
        request.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try {
                rpcRequest = serializer.deserialize(bytes, RpcRequest.class);
                if (rpcRequest != null) {
                    System.out.println("收到RPC请求: 服务名=" + rpcRequest.getServiceName() 
                                     + ", 方法名=" + rpcRequest.getMethodName());
                }
            } catch (Exception e) {
                System.err.println("请求反序列化失败: " + e.getMessage());
                e.printStackTrace();
            }

            //build the response
            RpcResponse rpcResponse = new RpcResponse();
            // if request is null, return error response
            if(rpcRequest == null){
                rpcResponse.setMessage("请求反序列化失败，request is null");
                doResponse(request, rpcResponse, serializer);
                return;
            }
            try {
                String serviceName = rpcRequest.getServiceName();
                System.out.println("正在查找服务: " + serviceName);
                
                //get the service
                Class<?> implClass = LocalRegistry.get(serviceName);
                
                if (implClass == null) {
                    System.err.println("服务未找到: " + serviceName);
                    System.out.println("已注册的服务列表: " + LocalRegistry.listServices());
                    rpcResponse.setMessage("服务未找到: " + serviceName);
                    doResponse(request, rpcResponse, serializer);
                    return;
                }
                
                System.out.println("找到服务实现类: " + implClass.getName());
                
                // 使用反射调用方法
                Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                System.out.println("找到方法: " + method.getName());
                
                Object serviceInstance = implClass.newInstance();
                System.out.println("实例化服务: " + serviceInstance.getClass().getName());
                
                Object result = method.invoke(serviceInstance, rpcRequest.getArgs());
                System.out.println("方法调用成功，返回结果类型: " + (result != null ? result.getClass().getName() : "null"));
                
                //set the result
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("success");
            } catch (Exception e) {
                System.err.println("处理请求时发生异常: " + e.getMessage());
                e.printStackTrace();
                rpcResponse.setMessage(e.getMessage());
                rpcResponse.setException(e);
            }
            doResponse(request, rpcResponse, serializer);
        });
    }
    
    /**
     * Do response to the client
     *
     * @param request Http request
     * @param rpcResponse RPC response
     * @param serializer Serializer
     */
    private void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse response = request.response();
        response.putHeader("content-type", "application/json");
        try {
            byte[] bytes = serializer.serialize(rpcResponse);//serialize the response
            System.out.println("发送响应: " + rpcResponse.getMessage());
            response.end(Buffer.buffer(bytes));
        } catch (Exception e) {
            System.err.println("序列化响应时发生异常: " + e.getMessage());
            e.printStackTrace();
            response.end(Buffer.buffer("error"));
        }
    }
}
