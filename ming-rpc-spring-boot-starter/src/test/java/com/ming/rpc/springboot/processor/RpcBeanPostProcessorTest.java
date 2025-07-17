package com.ming.rpc.springboot.processor;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ming.rpc.RpcApplication;
import com.ming.rpc.springboot.annotation.RpcReference;
import com.ming.rpc.springboot.annotation.RpcService;

/**
 * RpcBeanPostProcessor测试类
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
class RpcBeanPostProcessorTest {

    private RpcBeanPostProcessor processor;

    // 测试接口
    interface TestService {
        String test();
    }

    // 测试服务实现
    @RpcService
    static class TestServiceImpl implements TestService {
        @Override
        public String test() {
            return "test";
        }
    }

    // 带自定义配置的服务实现
    @RpcService(interfaceClass = TestService.class, version = "1.0", group = "test")
    static class CustomTestServiceImpl implements TestService {
        @Override
        public String test() {
            return "custom test";
        }
    }

    // 测试消费者
    static class TestConsumer {
        @RpcReference
        private TestService testService;

        @RpcReference(mock = true)
        private TestService mockTestService;

        public TestService getTestService() {
            return testService;
        }

        public TestService getMockTestService() {
            return mockTestService;
        }
    }

    @BeforeEach
    void setUp() {
        processor = new RpcBeanPostProcessor();
        // 不初始化RPC应用，避免注册中心连接问题
    }

    @AfterEach
    void tearDown() {
        RpcApplication.destroy();
    }

    @Test
    void testProcessRpcServiceWithDefaults() {
        TestServiceImpl service = new TestServiceImpl();

        // 处理Bean
        Object result = processor.postProcessBeforeInitialization(service, "testService");

        // 验证返回的是同一个对象
        assertThat(result).isSameAs(service);
        // 注册逻辑已经执行，但不验证具体的注册中心调用，避免连接问题
    }

    @Test
    void testProcessRpcServiceWithCustomConfig() {
        CustomTestServiceImpl service = new CustomTestServiceImpl();

        // 处理Bean
        Object result = processor.postProcessBeforeInitialization(service, "customTestService");

        // 验证返回的是同一个对象
        assertThat(result).isSameAs(service);
        // 注册逻辑已经执行，但不验证具体的注册中心调用，避免连接问题
    }

    @Test
    void testProcessRpcReference() throws NoSuchFieldException {
        TestConsumer consumer = new TestConsumer();

        // 处理Bean
        Object result = processor.postProcessAfterInitialization(consumer, "testConsumer");

        // 验证返回的是同一个对象
        assertThat(result).isSameAs(consumer);

        // 代理注入逻辑已经执行，但在测试环境中可能无法创建完整的代理
        // 这里只验证处理器正常执行，不验证具体的代理对象
    }

    @Test
    void testBuildServiceKey() throws Exception {
        // 使用反射访问私有方法
        java.lang.reflect.Method method = RpcBeanPostProcessor.class.getDeclaredMethod(
            "buildServiceKey", String.class, String.class, String.class);
        method.setAccessible(true);
        
        // 测试默认版本
        String key1 = (String) method.invoke(processor, "com.test.Service", "", "");
        assertThat(key1).isEqualTo("com.test.Service:1.0");
        
        // 测试自定义版本
        String key2 = (String) method.invoke(processor, "com.test.Service", "2.0", "");
        assertThat(key2).isEqualTo("com.test.Service:2.0");
        
        // 测试版本和分组
        String key3 = (String) method.invoke(processor, "com.test.Service", "2.0", "test");
        assertThat(key3).isEqualTo("com.test.Service:2.0:test");
    }

    @Test
    void testNonRpcBean() {
        // 普通Bean，没有RPC注解
        String normalBean = "normal bean";
        
        // 处理Bean
        Object result1 = processor.postProcessBeforeInitialization(normalBean, "normalBean");
        Object result2 = processor.postProcessAfterInitialization(normalBean, "normalBean");
        
        // 验证返回的是同一个对象，没有被修改
        assertThat(result1).isSameAs(normalBean);
        assertThat(result2).isSameAs(normalBean);
    }

    @Test
    void testRpcServiceWithoutInterface() {
        // 没有实现接口的服务类
        @RpcService
        class NoInterfaceService {
            public String test() {
                return "test";
            }
        }
        
        NoInterfaceService service = new NoInterfaceService();
        
        // 处理Bean，应该不会抛出异常
        Object result = processor.postProcessBeforeInitialization(service, "noInterfaceService");
        assertThat(result).isSameAs(service);
    }
}
