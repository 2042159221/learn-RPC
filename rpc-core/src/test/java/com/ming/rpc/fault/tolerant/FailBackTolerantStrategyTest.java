package com.ming.rpc.fault.tolerant;

import com.ming.rpc.RpcApplication;
import com.ming.rpc.config.RpcConfig;
import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务降级策略测试
 */
class FailBackTolerantStrategyTest {

    private final FailBackTolerantStrategy failBackStrategy = new FailBackTolerantStrategy();

    // 待测试的服务接口
    interface GreetingService {
        String sayHello(String name);
    }

    // 对应的降级服务实现
    static class GreetingServiceMock implements GreetingService {
        @Override
        public String sayHello(String name) {
            return "Mocked Greeting for " + name;
        }
    }

    // 未提供降级实现的服务接口
    interface UnmockedService {
        void doNothing();
    }

    @BeforeAll
    static void beforeAll() {
        // 初始化 RpcApplication 和配置，并注册 Mock 服务
        RpcApplication.init();
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        rpcConfig.getMockServiceRegistry().put(GreetingService.class.getName(), GreetingServiceMock.class);
    }

    @Test
    void testDoTolerant_SuccessWithMock() {
        // 安排
        Map<String, Object> context = new HashMap<>();
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName(GreetingService.class.getName());
        rpcRequest.setMethodName("sayHello");
        rpcRequest.setParameterTypes(new Class[]{String.class});
        rpcRequest.setArgs(new Object[]{"World"});
        context.put("rpcRequest", rpcRequest);

        // 行动
        RpcResponse response = failBackStrategy.doTolerant(context, new RuntimeException("Test exception"));

        // 断言
        Assertions.assertNotNull(response);
        Assertions.assertEquals(RpcResponse.MessageType.SUCCESS, response.getMessageType());
        Assertions.assertEquals("Mocked Greeting for World", response.getData());
        Assertions.assertEquals("Fallback success", response.getMessage());
    }

    @Test
    void testDoTolerant_NoMockFound() {
        // 安排
        Map<String, Object> context = new HashMap<>();
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setServiceName(UnmockedService.class.getName());
        rpcRequest.setMethodName("doNothing");
        context.put("rpcRequest", rpcRequest);

        // 行动
        RpcResponse response = failBackStrategy.doTolerant(context, new RuntimeException("Test exception"));

        // 断言
        Assertions.assertNotNull(response);
        Assertions.assertNull(response.getData()); // 表现为静默失败
    }
} 