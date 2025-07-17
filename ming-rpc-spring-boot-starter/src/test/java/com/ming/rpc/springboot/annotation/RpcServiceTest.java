package com.ming.rpc.springboot.annotation;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RpcService注解测试类
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
class RpcServiceTest {

    interface TestService {
        String test();
    }

    @RpcService
    static class TestServiceWithDefaults implements TestService {
        @Override
        public String test() {
            return "test";
        }
    }

    @RpcService(
        interfaceClass = TestService.class,
        version = "1.0",
        group = "test-group",
        tags = {"tag1", "tag2"}
    )
    static class TestServiceWithCustomConfig implements TestService {
        @Override
        public String test() {
            return "test";
        }
    }

    @Test
    void testRpcServiceAnnotationExists() {
        // 验证注解存在
        RpcService annotation = TestServiceWithDefaults.class.getAnnotation(RpcService.class);
        assertThat(annotation).isNotNull();
    }

    @Test
    void testDefaultValues() {
        RpcService annotation = TestServiceWithDefaults.class.getAnnotation(RpcService.class);
        
        // 验证默认值
        assertThat(annotation.interfaceClass()).isEqualTo(void.class);
        assertThat(annotation.version()).isEmpty();
        assertThat(annotation.group()).isEmpty();
        assertThat(annotation.tags()).isEmpty();
    }

    @Test
    void testCustomValues() {
        RpcService annotation = TestServiceWithCustomConfig.class.getAnnotation(RpcService.class);
        
        // 验证自定义值
        assertThat(annotation.interfaceClass()).isEqualTo(TestService.class);
        assertThat(annotation.version()).isEqualTo("1.0");
        assertThat(annotation.group()).isEqualTo("test-group");
        assertThat(annotation.tags()).containsExactly("tag1", "tag2");
    }

    @Test
    void testAnnotationMetadata() {
        // 验证注解元数据
        assertThat(RpcService.class.isAnnotationPresent(java.lang.annotation.Target.class)).isTrue();
        assertThat(RpcService.class.isAnnotationPresent(java.lang.annotation.Retention.class)).isTrue();
        assertThat(RpcService.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        assertThat(RpcService.class.isAnnotationPresent(Component.class)).isTrue();
    }

    @Test
    void testComponentAnnotation() {
        // 验证@Component注解存在，确保被Spring管理
        Component componentAnnotation = RpcService.class.getAnnotation(Component.class);
        assertThat(componentAnnotation).isNotNull();
    }

    @Test
    void testServiceImplementsInterface() {
        // 验证测试服务类实现了接口
        assertThat(TestService.class.isAssignableFrom(TestServiceWithDefaults.class)).isTrue();
        assertThat(TestService.class.isAssignableFrom(TestServiceWithCustomConfig.class)).isTrue();
    }
}
