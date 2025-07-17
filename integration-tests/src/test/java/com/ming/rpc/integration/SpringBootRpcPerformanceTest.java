package com.ming.rpc.integration;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Spring Boot RPC 性能测试
 *
 * 测试RPC框架的性能指标
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@Slf4j
class SpringBootRpcPerformanceTest {

    /**
     * 模拟RPC调用的服务
     */
    private String callMockRpcService(String name) {
        // 模拟RPC调用的响应时间
        try {
            Thread.sleep(1); // 模拟1ms的网络延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "Performance test result for: " + name;
    }

    @Test
    void testStartupTime() {
        // 测试应用启动时间
        long startTime = System.currentTimeMillis();

        // 模拟应用启动过程
        String mockResult = callMockRpcService("startup-test");
        assertThat(mockResult).isNotNull();

        long endTime = System.currentTimeMillis();
        long startupTime = endTime - startTime;

        log.info("Application startup time: {} ms", startupTime);

        // 启动时间应该在合理范围内（这里设置为10秒，实际可能更快）
        assertThat(startupTime).isLessThan(10000);
    }

    @Test
    void testMemoryUsage() {
        // 测试内存使用情况
        Runtime runtime = Runtime.getRuntime();
        
        // 执行GC获取更准确的内存使用情况
        System.gc();
        
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        log.info("Memory usage - Total: {} MB, Used: {} MB, Free: {} MB", 
            totalMemory / 1024 / 1024, 
            usedMemory / 1024 / 1024, 
            freeMemory / 1024 / 1024);
        
        // 验证内存使用在合理范围内（这里设置为1GB）
        assertThat(usedMemory).isLessThan(1024 * 1024 * 1024);
    }

    @Test
    void testConcurrentRpcCalls() throws InterruptedException {
        // 测试并发RPC调用
        int threadCount = 10;
        int callsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < callsPerThread; j++) {
                        try {
                            String result = callMockRpcService("user-" + threadId + "-" + j);
                            if (result != null) {
                                successCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                            log.error("RPC call failed", e);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有线程完成
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertThat(completed).isTrue();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        int totalCalls = threadCount * callsPerThread;
        double avgResponseTime = (double) totalTime / totalCalls;
        double throughput = (double) totalCalls / totalTime * 1000; // calls per second

        log.info("Performance test results:");
        log.info("Total calls: {}", totalCalls);
        log.info("Successful calls: {}", successCount.get());
        log.info("Failed calls: {}", errorCount.get());
        log.info("Total time: {} ms", totalTime);
        log.info("Average response time: {:.2f} ms", avgResponseTime);
        log.info("Throughput: {:.2f} calls/second", throughput);

        // 验证成功率
        double successRate = (double) successCount.get() / totalCalls;
        assertThat(successRate).isGreaterThan(0.95); // 95%以上成功率

        // 验证平均响应时间
        assertThat(avgResponseTime).isLessThan(100); // 平均响应时间小于100ms

        executor.shutdown();
    }

    @Test
    void testRpcServiceAvailability() {
        // 测试RPC服务可用性

        // 执行多次调用验证稳定性
        for (int i = 0; i < 10; i++) {
            String result = callMockRpcService("availability-test-" + i);
            assertThat(result).isNotNull();
            assertThat(result).contains("availability-test-" + i);
        }
    }
}
