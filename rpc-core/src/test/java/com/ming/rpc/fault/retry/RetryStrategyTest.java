package com.ming.rpc.fault.retry;

import com.github.rholder.retry.RetryException;
import com.ming.rpc.model.RpcResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 重试策略测试
 */
public class RetryStrategyTest {

    @Test
    @DisplayName("NoRetryStrategy: 成功调用测试")
    void testNoRetrySuccess() throws Exception {
        RetryStrategy retryStrategy = new NoRetryStrategy();
        final AtomicInteger callCount = new AtomicInteger(0);
        Callable<RpcResponse> callable = () -> {
            callCount.incrementAndGet();
            return RpcResponse.builder().message("success").build();
        };

        RpcResponse response = retryStrategy.doRetry(callable);
        assertEquals("success", response.getMessage());
        assertEquals(1, callCount.get());
    }

    @Test
    @DisplayName("NoRetryStrategy: 失败调用测试")
    void testNoRetryFailure() {
        RetryStrategy retryStrategy = new NoRetryStrategy();
        final AtomicInteger callCount = new AtomicInteger(0);
        Callable<RpcResponse> callable = () -> {
            callCount.incrementAndGet();
            throw new RuntimeException("failed");
        };

        Exception exception = assertThrows(RuntimeException.class, () -> retryStrategy.doRetry(callable));
        assertEquals("failed", exception.getMessage());
        assertEquals(1, callCount.get());
    }

    @Test
    @DisplayName("FixedIntervalRetryStrategy: 首次成功测试")
    void testFixedIntervalSuccess() throws Exception {
        RetryStrategy retryStrategy = new FixedIntervalRetryStrategy();
        final AtomicInteger callCount = new AtomicInteger(0);
        Callable<RpcResponse> callable = () -> {
            callCount.incrementAndGet();
            return RpcResponse.builder().message("success").build();
        };

        RpcResponse response = retryStrategy.doRetry(callable);
        assertEquals("success", response.getMessage());
        assertEquals(1, callCount.get());
    }

    @Test
    @DisplayName("FixedIntervalRetryStrategy: 重试后成功测试")
    void testFixedIntervalRetryAndSucceed() throws Exception {
        RetryStrategy retryStrategy = new FixedIntervalRetryStrategy();
        final AtomicInteger callCount = new AtomicInteger(0);
        Callable<RpcResponse> callable = () -> {
            int count = callCount.incrementAndGet();
            if (count < 2) {
                System.out.println("Simulating failure on attempt " + count);
                throw new RuntimeException("transient failure");
            }
            System.out.println("Simulating success on attempt " + count);
            return RpcResponse.builder().message("success").build();
        };

        RpcResponse response = retryStrategy.doRetry(callable);
        assertEquals("success", response.getMessage());
        assertEquals(2, callCount.get());
    }

    @Test
    @DisplayName("FixedIntervalRetryStrategy: 重试后最终失败测试")
    void testFixedIntervalRetryAndFail() {
        RetryStrategy retryStrategy = new FixedIntervalRetryStrategy();
        final AtomicInteger callCount = new AtomicInteger(0);
        Callable<RpcResponse> callable = () -> {
            callCount.incrementAndGet();
            throw new RuntimeException("persistent failure");
        };

        Exception exception = assertThrows(RetryException.class, () -> retryStrategy.doRetry(callable));
        // RetryException 的根本原因应该是我们最初的异常
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("persistent failure", exception.getCause().getMessage());
        // 策略配置为尝试 3 次
        assertEquals(3, callCount.get());
    }
    
    @Test
    @DisplayName("RetryStrategyFactory: 工厂加载测试")
    void testFactory() {
        RetryStrategy noRetry = RetryStrategyFactory.getInstance(RetryStrategyKeys.NO);
        assertTrue(noRetry instanceof NoRetryStrategy, "工厂应该为 'no' 键返回 NoRetryStrategy 实例");

        RetryStrategy fixedInterval = RetryStrategyFactory.getInstance(RetryStrategyKeys.FIXED_INTERVAL);
        assertTrue(fixedInterval instanceof FixedIntervalRetryStrategy, "工厂应该为 'fixedInterval' 键返回 FixedIntervalRetryStrategy 实例");
    }
} 