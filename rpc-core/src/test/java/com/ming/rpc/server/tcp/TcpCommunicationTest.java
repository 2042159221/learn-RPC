package com.ming.rpc.server.tcp;

import com.ming.rpc.RpcApplication;
import com.ming.rpc.config.RpcConfig;
import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.constant.RpcConstant;
import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
import com.ming.rpc.model.ServiceMetaInfo;
import com.ming.rpc.registry.LocalRegistry;
import com.ming.rpc.serializer.JdkSerializer;
import com.ming.rpc.serializer.Serializer;
import com.ming.rpc.serializer.SerializerFactory;
import com.ming.rpc.spi.SpiLoader;
import com.ming.rpc.utils.ConfigUtils;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * TCP通信模块单元测试
 */
public class TcpCommunicationTest {

    // 测试用端口
    private static final int TEST_PORT = 8890;
    // 测试用主机
    private static final String TEST_HOST = "127.0.0.1";

    private Vertx vertx;
    private NetServer netServer;

    /**
     * 测试前准备：启动服务端
     */
    @BeforeEach
    public void setUp() {
        try {
            // 确保序列化器被加载
            SpiLoader.load(Serializer.class);
            
            // 初始化RPC应用配置
            initRpcConfig();
            
            // 创建Vert.x实例
            vertx = Vertx.vertx();
            // 创建并启动TCP服务器
            netServer = vertx.createNetServer();
            // 注册服务
            registerTestService();
            
            // 启动服务器
            CompletableFuture<Void> future = new CompletableFuture<>();
            netServer.connectHandler(new TcpServerHandler())
                    .listen(TEST_PORT, ar -> {
                        if (ar.succeeded()) {
                            System.out.println("测试服务器启动成功，监听端口：" + TEST_PORT);
                            future.complete(null);
                        } else {
                            System.err.println("测试服务器启动失败：" + ar.cause().getMessage());
                            future.completeExceptionally(ar.cause());
                        }
                    });
            
            future.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println("测试启动失败：" + e.getMessage());
            e.printStackTrace();
            // 确保资源释放
            tearDown();
            // 将异常转换为运行时异常重新抛出
            throw new RuntimeException("测试启动失败", e);
        }
    }

    /**
     * 初始化RPC应用配置
     * 
     * @throws ReflectiveOperationException 如果反射操作失败
     */
    private void initRpcConfig() throws ReflectiveOperationException {
        // 手动创建并设置RPC配置，而不是从文件加载
        RpcConfig rpcConfig = new RpcConfig();
        rpcConfig.setName("ming-rpc-test");
        rpcConfig.setVersion("1.0");
        rpcConfig.setServerHost(TEST_HOST);
        rpcConfig.setServerPort(TEST_PORT);
        rpcConfig.setSerializer("jdk");  // 使用小写的"jdk"与SPI配置匹配
        rpcConfig.setLoadBalancer("ROUND_ROBIN");
        rpcConfig.setRetryStrategy("NO");
        rpcConfig.setTolerantStrategy("FAIL_FAST");
        rpcConfig.setMock(true);
        
        // 手动设置到RpcApplication中
        setRpcConfig(rpcConfig);
        
        // 验证序列化器是否正确加载
        Serializer serializer = SerializerFactory.getInstance(rpcConfig.getSerializer());
        if (serializer == null) {
            // 如果通过SPI加载失败，则手动添加JDK序列化器到SPI加载器
            System.out.println("警告：通过SPI加载序列化器失败，手动添加JDK序列化器");
            serializer = new JdkSerializer();
            
            // 手动添加JDK序列化器到SpiLoader中
            try {
                // 获取loaderMap字段
                Field loaderMapField = SpiLoader.class.getDeclaredField("loaderMap");
                loaderMapField.setAccessible(true);
                Map<String, Map<String, Class<?>>> loaderMap = (Map<String, Map<String, Class<?>>>) loaderMapField.get(null);
                
                // 获取或创建Serializer的映射
                Map<String, Class<?>> serializerMap = loaderMap.computeIfAbsent(Serializer.class.getName(), k -> new HashMap<>());
                serializerMap.put("jdk", JdkSerializer.class);
                
                // 获取instanceCache字段
                Field instanceCacheField = SpiLoader.class.getDeclaredField("instanceCache");
                instanceCacheField.setAccessible(true);
                Map<String, Object> instanceCache = (Map<String, Object>) instanceCacheField.get(null);
                
                // 添加实例到缓存
                instanceCache.put(JdkSerializer.class.getName(), serializer);
            } catch (Exception e) {
                System.err.println("手动添加JDK序列化器失败：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("成功加载序列化器：" + serializer.getClass().getName());
        }
        
        System.out.println("RPC配置初始化成功: " + rpcConfig);
    }
    
    /**
     * 使用反射设置RpcConfig，避免调用init方法初始化注册中心
     * 
     * @param rpcConfig 要设置的RPC配置
     * @throws ReflectiveOperationException 如果反射操作失败
     */
    private void setRpcConfig(RpcConfig rpcConfig) throws ReflectiveOperationException {
        Field configField = RpcApplication.class.getDeclaredField("rpcConfig");
        configField.setAccessible(true);
        configField.set(null, rpcConfig);
    }

    /**
     * 注册测试服务
     */
    private void registerTestService() {
        LocalRegistry.register("testService", TestServiceImpl.class);
    }

    /**
     * 测试后清理：关闭服务端
     */
    @AfterEach
    public void tearDown() {
        // 关闭服务器
        if (netServer != null) {
            netServer.close();
        }
        // 关闭Vertx实例
        if (vertx != null) {
            vertx.close();
        }
        // 清除注册服务
        LocalRegistry.remove("testService");
        
        // 销毁RPC应用，清理资源
        RpcApplication.destroy();
    }

    /**
     * 测试TCP客户端发送请求并接收响应
     */
    @Test
    @DisplayName("测试TCP客户端请求和响应")
    public void testTcpClientRequest() throws ExecutionException, InterruptedException {
        try {
            // 创建服务元信息
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
            serviceMetaInfo.setServiceHost(TEST_HOST);
            serviceMetaInfo.setServicePort(TEST_PORT);
            serviceMetaInfo.setServiceName("testService");
            
            // 创建RPC请求
            RpcRequest rpcRequest = new RpcRequest();
            rpcRequest.setServiceName("testService");
            rpcRequest.setMethodName("testMethod");
            rpcRequest.setParameterTypes(new Class[]{String.class});
            rpcRequest.setArgs(new Object[]{"test"});
            
            // 发送请求并获取响应
            RpcResponse rpcResponse = VertexTcpClient.doRequest(rpcRequest, serviceMetaInfo);
            
            // 验证响应
            assertNotNull(rpcResponse, "响应不能为空");
            assertEquals("ok", rpcResponse.getMessage(), "响应消息错误");
            assertEquals("test response", rpcResponse.getData(), "响应数据错误");
        } catch (Exception e) {
            System.err.println("测试执行失败，详细错误：" + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 测试服务接口
     */
    public interface TestService extends Serializable {
        String testMethod(String param);
    }
    
    /**
     * 测试服务实现类
     */
    public static class TestServiceImpl implements TestService {
        private static final long serialVersionUID = 1L;
        
        @Override
        public String testMethod(String param) {
            return param + " response";
        }
    }
} 