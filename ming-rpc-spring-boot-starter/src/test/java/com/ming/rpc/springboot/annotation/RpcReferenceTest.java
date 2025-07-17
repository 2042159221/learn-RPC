package com.ming.rpc.springboot.annotation;

import com.ming.rpc.fault.retry.RetryStrategyKeys;
import com.ming.rpc.fault.tolerant.TolerantStrategyKeys;
import com.ming.rpc.loadbalancer.LoadBalancerKeys;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RpcReference注解测试类
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
class RpcReferenceTest {

    interface TestService {
        String test();
    }

    static class TestConsumer {
        @RpcReference
        private TestService defaultService;

        @RpcReference(
            interfaceClass = TestService.class,
            version = "1.0",
            group = "test-group",
            loadBalancer = LoadBalancerKeys.RANDOM,
            retryStrategy = RetryStrategyKeys.FIXED_INTERVAL,
            tolerantStrategy = TolerantStrategyKeys.FAIL_OVER,
            mock = true,
            timeout = 5000L
        )
        private TestService customService;
    }

    @Test
    void testRpcReferenceAnnotationExists() throws NoSuchFieldException {
        // 验证注解存在
        Field field = TestConsumer.class.getDeclaredField("defaultService");
        RpcReference annotation = field.getAnnotation(RpcReference.class);
        assertThat(annotation).isNotNull();
    }

    @Test
    void testDefaultValues() throws NoSuchFieldException {
        Field field = TestConsumer.class.getDeclaredField("defaultService");
        RpcReference annotation = field.getAnnotation(RpcReference.class);
        
        // 验证默认值
        assertThat(annotation.interfaceClass()).isEqualTo(void.class);
        assertThat(annotation.version()).isEmpty();
        assertThat(annotation.group()).isEmpty();
        assertThat(annotation.loadBalancer()).isEqualTo(LoadBalancerKeys.ROUND_ROBIN);
        assertThat(annotation.retryStrategy()).isEqualTo(RetryStrategyKeys.NO);
        assertThat(annotation.tolerantStrategy()).isEqualTo(TolerantStrategyKeys.FAIL_FAST);
        assertThat(annotation.mock()).isFalse();
        assertThat(annotation.timeout()).isEqualTo(3000L);
    }

    @Test
    void testCustomValues() throws NoSuchFieldException {
        Field field = TestConsumer.class.getDeclaredField("customService");
        RpcReference annotation = field.getAnnotation(RpcReference.class);
        
        // 验证自定义值
        assertThat(annotation.interfaceClass()).isEqualTo(TestService.class);
        assertThat(annotation.version()).isEqualTo("1.0");
        assertThat(annotation.group()).isEqualTo("test-group");
        assertThat(annotation.loadBalancer()).isEqualTo(LoadBalancerKeys.RANDOM);
        assertThat(annotation.retryStrategy()).isEqualTo(RetryStrategyKeys.FIXED_INTERVAL);
        assertThat(annotation.tolerantStrategy()).isEqualTo(TolerantStrategyKeys.FAIL_OVER);
        assertThat(annotation.mock()).isTrue();
        assertThat(annotation.timeout()).isEqualTo(5000L);
    }

    @Test
    void testAnnotationMetadata() {
        // 验证注解元数据
        assertThat(RpcReference.class.isAnnotationPresent(java.lang.annotation.Target.class)).isTrue();
        assertThat(RpcReference.class.isAnnotationPresent(java.lang.annotation.Retention.class)).isTrue();
        assertThat(RpcReference.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
    }

    @Test
    void testFieldTypes() throws NoSuchFieldException {
        // 验证字段类型
        Field defaultField = TestConsumer.class.getDeclaredField("defaultService");
        Field customField = TestConsumer.class.getDeclaredField("customService");
        
        assertThat(defaultField.getType()).isEqualTo(TestService.class);
        assertThat(customField.getType()).isEqualTo(TestService.class);
    }
}
