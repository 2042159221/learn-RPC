package com.ming.rpc.server.http;

import java.lang.reflect.Method;
import java.io.IOException;

import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
import com.ming.rpc.RpcApplication;
import com.ming.rpc.serializer.Serializer;
import com.ming.rpc.serializer.SerializerFactory;
import com.ming.rpc.registry.LocalRegistry;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

/**
 * HTTP 请求处理器
 */
public class HttpServerHandler  implements Handler<HttpServerRequest>{
    @Override
    public void handle(HttpServerRequest request) {
        //指定序列化器
        final Serializer serializer = SerializerFactory.getInstance(RpcApplication.getRpcConfig().getSerializer());
        // 记录日志
        System.out.println("Received request: " +  request.method() + " " + request.uri());

        //异步处理HTTP请求
        Serializer finalSerializer = serializer;
        request.bodyHandler(body -> {
            byte[] bytes = body.getBytes();
            RpcRequest rpcRequest = null;
            try{
                rpcRequest = finalSerializer.deserialize(bytes, RpcRequest.class);
            }catch(Exception e){
                e.printStackTrace();
                request.response().setStatusCode(500).end("Internal Server Error");
                return;
            }

            //构造响应结果对象
            RpcResponse rpcResponse = new RpcResponse();
            //如果请求为NULL，直接返回
            if (rpcRequest == null) {
                rpcResponse.setMessage("Request is null");
                doResponse(request, rpcResponse, finalSerializer);
                return;
        }
        try{
            //获取要调用的服务实现类，通过反射调用
            Class<?> implClass = LocalRegistry.get(rpcRequest.getServiceName());
            Method method = implClass.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            Object result = method.invoke(implClass.newInstance(), rpcRequest.getArgs());
            //封装返回结果
            rpcResponse.setData(result);
            rpcResponse.setDataType(method.getReturnType());
            rpcResponse.setMessage("ok");
            rpcResponse.setMessageType(RpcResponse.MessageType.SUCCESS);
        }catch(Exception e){
            e.printStackTrace();
            rpcResponse.setMessage("Internal Server Error");
            rpcResponse.setMessageType(RpcResponse.MessageType.FAILURE);
        }
        //响应
        doResponse(request, rpcResponse, finalSerializer);
    });
    }

    /**
     * 响应
     * @param request
     * @param rpcResponse
     * @param serializer
     */
    private void doResponse(HttpServerRequest request, RpcResponse rpcResponse, Serializer serializer) {
        HttpServerResponse httpServerResponse = request.response().putHeader("content-type", "application/json");
        try{
            //序列化
            byte[] serialized = serializer.serialize(rpcResponse);
            httpServerResponse.end(Buffer.buffer(serialized));
        }catch(IOException e){
            e.printStackTrace();
            httpServerResponse.setStatusCode(500).end("Internal Server Error");
        }
    }
}
