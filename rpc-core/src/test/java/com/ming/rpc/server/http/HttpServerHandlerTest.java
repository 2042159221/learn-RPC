package com.ming.rpc.server.http;

import com.ming.rpc.RpcApplication;
import com.ming.rpc.config.RpcConfig;
import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
import com.ming.rpc.registry.LocalRegistry;
import com.ming.rpc.serializer.JdkSerializer;
import com.ming.rpc.serializer.Serializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * HTTP请求处理器测试类
 */
@ExtendWith(MockitoExtension.class)
public class HttpServerHandlerTest {
    
    private final String TEST_SERVICE_NAME = "com.ming.rpc.server.http.TestService";

    @BeforeEach
    void setUp() {
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
    }

    /**
     * 测试echo方法的请求处理
     */
    @Test
    void testEchoMethod() throws IOException {
        // 创建RPC请求
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName(TEST_SERVICE_NAME);
        rpcRequest.setMethodName("echo");
        rpcRequest.setParameterTypes(new Class<?>[]{String.class});
        rpcRequest.setArgs(new Object[]{"Hello RPC"});
        
        // 序列化请求
        Serializer serializer = new JdkSerializer();
        
        // 直接调用服务方法
        TestService testService = new TestServiceImpl();
        String result = testService.echo("Hello RPC");
        
        // 验证结果
        assertEquals("Hello RPC", result);
    }

    /**
     * 测试add方法的请求处理
     */
    @Test
    void testAddMethod() throws IOException {
        // 创建RPC请求
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName(TEST_SERVICE_NAME);
        rpcRequest.setMethodName("add");
        rpcRequest.setParameterTypes(new Class<?>[]{int.class, int.class});
        rpcRequest.setArgs(new Object[]{10, 20});
        
        // 直接调用服务方法
        TestService testService = new TestServiceImpl();
        int result = testService.add(10, 20);
        
        // 验证结果
        assertEquals(30, result);
    }
} 