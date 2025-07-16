package com.ming.rpc.server.http;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.ming.rpc.RpcApplication;
import com.ming.rpc.config.RpcConfig;
import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
import com.ming.rpc.registry.LocalRegistry;
import com.ming.rpc.serializer.JdkSerializer;
import com.ming.rpc.serializer.Serializer;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(VertxExtension.class)
public class VertxHttpServerTest {

    private Vertx vertx;
    private WebClient client;
    private VertxHttpServer server;
    private final int TEST_PORT = 8888;
    private final String TEST_SERVICE_NAME = "com.ming.rpc.server.http.TestService";

    @BeforeEach
    void setUp() {
        // 初始化Vertx实例
        vertx = Vertx.vertx();
        client = WebClient.create(vertx);
        
        // 注册测试服务
        LocalRegistry.register(TEST_SERVICE_NAME, TestServiceImpl.class);
        
        // 初始化RPC配置
        RpcConfig rpcConfig = new RpcConfig();
        rpcConfig.setSerializer("jdk");
        
        // 使用mock注册中心
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setRegistry("mock");
        rpcConfig.setRegistryConfig(registryConfig);
        
        RpcApplication.init(rpcConfig);
        
        // 创建并启动HTTP服务器
        server = new VertxHttpServer();
        server.doStart(TEST_PORT);
        
        // 等待服务器启动
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown(VertxTestContext testContext) {
        // 关闭客户端和Vertx实例
        client.close();
        vertx.close(testContext.succeedingThenComplete());
        
        // 清理注册的服务
        LocalRegistry.remove(TEST_SERVICE_NAME);
    }

    @Test
    void testServerStart() throws Exception {
        // 测试服务器是否成功启动
        CountDownLatch latch = new CountDownLatch(1);
        
        client.get(TEST_PORT, "localhost", "/")
            .send()
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<Buffer> response = ar.result();
                    // 即使是404，只要服务器响应了就表示启动成功
                    latch.countDown();
                } else {
                    ar.cause().printStackTrace();
                }
            });
        
        // 等待响应
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testEchoMethod() throws Exception {
        // 创建RPC请求
        RpcRequest request = new RpcRequest();
        request.setServiceName(TEST_SERVICE_NAME);
        request.setMethodName("echo");
        request.setParameterTypes(new Class<?>[]{String.class});
        request.setArgs(new Object[]{"Hello RPC"});
        
        // 序列化请求
        Serializer serializer = new JdkSerializer();
        byte[] data = serializer.serialize(request);
        
        // 发送请求并等待响应
        CountDownLatch latch = new CountDownLatch(1);
        
        client.request(HttpMethod.POST, TEST_PORT, "localhost", "/")
            .sendBuffer(Buffer.buffer(data))
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<Buffer> response = ar.result();
                    try {
                        // 反序列化响应
                        RpcResponse rpcResponse = serializer.deserialize(response.bodyAsBuffer().getBytes(), RpcResponse.class);
                        
                        // 验证响应
                        assertNotNull(rpcResponse);
                        assertEquals("Hello RPC", rpcResponse.getData());
                        assertEquals(String.class, rpcResponse.getDataType());
                        assertEquals(RpcResponse.MessageType.SUCCESS, rpcResponse.getMessageType());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                } else {
                    ar.cause().printStackTrace();
                    latch.countDown();
                }
            });
        
        // 等待响应
        latch.await(5, TimeUnit.SECONDS);
    }

    @Test
    void testAddMethod() throws Exception {
        // 创建RPC请求
        RpcRequest request = new RpcRequest();
        request.setServiceName(TEST_SERVICE_NAME);
        request.setMethodName("add");
        request.setParameterTypes(new Class<?>[]{int.class, int.class});
        request.setArgs(new Object[]{10, 20});
        
        // 序列化请求
        Serializer serializer = new JdkSerializer();
        byte[] data = serializer.serialize(request);
        
        // 发送请求并等待响应
        CountDownLatch latch = new CountDownLatch(1);
        
        client.request(HttpMethod.POST, TEST_PORT, "localhost", "/")
            .sendBuffer(Buffer.buffer(data))
            .onComplete(ar -> {
                if (ar.succeeded()) {
                    HttpResponse<Buffer> response = ar.result();
                    try {
                        // 反序列化响应
                        RpcResponse rpcResponse = serializer.deserialize(response.bodyAsBuffer().getBytes(), RpcResponse.class);
                        
                        // 验证响应
                        assertNotNull(rpcResponse);
                        assertEquals(30, rpcResponse.getData());
                        assertEquals(int.class, rpcResponse.getDataType());
                        assertEquals(RpcResponse.MessageType.SUCCESS, rpcResponse.getMessageType());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                } else {
                    ar.cause().printStackTrace();
                    latch.countDown();
                }
            });
        
        // 等待响应
        latch.await(5, TimeUnit.SECONDS);
    }
} 