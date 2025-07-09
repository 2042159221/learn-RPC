package com.ming.rpc.client.transport;

import com.ming.rpc.model.request.RpcRequest;
import com.ming.rpc.model.response.RpcResponse;
import com.ming.rpc.protocol.serialize.JdkSerializer;
import com.ming.rpc.protocol.serialize.Serializer;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.util.concurrent.CompletableFuture;

/**
 * HTTP 客户端
 * 用于向服务端发送请求并获取响应
 */
public class HttpClient {

    private final WebClient webClient;
    private final Serializer serializer;

    /**
     * 默认构造方法
     */
    public HttpClient() {
        // 创建Vertx实例
        Vertx vertx = Vertx.vertx();
        
        // 配置WebClient选项
        WebClientOptions options = new WebClientOptions();
        options.setKeepAlive(true);
        options.setConnectTimeout(5000); // 连接超时时间
        
        // 创建WebClient
        this.webClient = WebClient.create(vertx, options);
        
        // 设置序列化器
        this.serializer = new JdkSerializer();
    }

    /**
     * 发送HTTP请求
     * @param rpcRequest RPC请求对象
     * @param host 服务器主机名
     * @param port 服务器端口
     * @return RPC响应对象
     */
    public RpcResponse sendRequest(RpcRequest rpcRequest, String host, int port) {
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        
        try {
            // 序列化请求
            byte[] requestBytes = serializer.serialize(rpcRequest);
            
            // 发送POST请求
            webClient.request(HttpMethod.POST, port, host, "/")
                    .sendBuffer(Buffer.buffer(requestBytes), ar -> {
                        if (ar.succeeded()) {
                            try {
                                // 获取响应内容
                                Buffer responseBuffer = ar.result().body();
                                // 反序列化响应
                                RpcResponse rpcResponse = serializer.deserialize(
                                        responseBuffer.getBytes(), 
                                        RpcResponse.class
                                );
                                // 完成Future
                                responseFuture.complete(rpcResponse);
                            } catch (Exception e) {
                                // 处理异常
                                responseFuture.completeExceptionally(e);
                            }
                        } else {
                            // 处理请求失败
                            responseFuture.completeExceptionally(ar.cause());
                        }
                    });
            
            // 等待响应
            return responseFuture.get();
        } catch (Exception e) {
            // 创建错误响应
            RpcResponse errorResponse = new RpcResponse();
            errorResponse.setException(e);
            errorResponse.setMessage("客户端请求异常: " + e.getMessage());
            return errorResponse;
        }
    }
} 