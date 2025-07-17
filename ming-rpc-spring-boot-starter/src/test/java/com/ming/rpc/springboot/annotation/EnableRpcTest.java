package com.ming.rpc.springboot.annotation;

import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * EnableRpc注解测试类
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
class EnableRpcTest {

    @EnableRpc
    static class TestClassWithDefaults {
    }

    @EnableRpc(needServer = false, basePackages = {"com.ming.test"})
    static class TestClassWithCustomConfig {
    }

    @EnableRpc(basePackageClasses = {EnableRpcTest.class})
    static class TestClassWithBasePackageClasses {
    }

    @Test
    void testEnableRpcAnnotationExists() {
        // 验证注解存在
        EnableRpc annotation = TestClassWithDefaults.class.getAnnotation(EnableRpc.class);
        assertThat(annotation).isNotNull();
    }

    @Test
    void testDefaultValues() {
        EnableRpc annotation = TestClassWithDefaults.class.getAnnotation(EnableRpc.class);
        
        // 验证默认值
        assertThat(annotation.needServer()).isTrue();
        assertThat(annotation.basePackages()).isEmpty();
        assertThat(annotation.basePackageClasses()).isEmpty();
    }

    @Test
    void testCustomValues() {
        EnableRpc annotation = TestClassWithCustomConfig.class.getAnnotation(EnableRpc.class);
        
        // 验证自定义值
        assertThat(annotation.needServer()).isFalse();
        assertThat(annotation.basePackages()).containsExactly("com.ming.test");
        assertThat(annotation.basePackageClasses()).isEmpty();
    }

    @Test
    void testBasePackageClasses() {
        EnableRpc annotation = TestClassWithBasePackageClasses.class.getAnnotation(EnableRpc.class);
        
        // 验证basePackageClasses配置
        assertThat(annotation.basePackageClasses()).containsExactly(EnableRpcTest.class);
    }

    @Test
    void testAnnotationMetadata() {
        // 验证注解元数据
        assertThat(EnableRpc.class.isAnnotationPresent(java.lang.annotation.Target.class)).isTrue();
        assertThat(EnableRpc.class.isAnnotationPresent(java.lang.annotation.Retention.class)).isTrue();
        assertThat(EnableRpc.class.isAnnotationPresent(java.lang.annotation.Documented.class)).isTrue();
        assertThat(EnableRpc.class.isAnnotationPresent(org.springframework.context.annotation.Import.class)).isTrue();
    }

    @Test
    void testImportConfiguration() {
        // 验证Import注解配置
        org.springframework.context.annotation.Import importAnnotation = 
            EnableRpc.class.getAnnotation(org.springframework.context.annotation.Import.class);
        
        assertThat(importAnnotation).isNotNull();
        assertThat(importAnnotation.value()).hasSize(3);
    }
}
