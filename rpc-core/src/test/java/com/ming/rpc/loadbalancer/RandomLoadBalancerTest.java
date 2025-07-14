package com.ming.rpc.loadbalancer;

import com.ming.rpc.model.ServiceMetaInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 随机负载均衡器测试
 */
public class RandomLoadBalancerTest {

    private final RandomLoadBalancer loadBalancer = new RandomLoadBalancer();

    private ServiceMetaInfo service1;
    private ServiceMetaInfo service2;
    private ServiceMetaInfo service3;

    @Before
    public void setUp() {
        service1 = new ServiceMetaInfo();
        service1.setServiceName("service1");
        service1.setServiceHost("localhost");
        service1.setServicePort(8080);

        service2 = new ServiceMetaInfo();
        service2.setServiceName("service2");
        service2.setServiceHost("localhost");
        service2.setServicePort(8081);

        service3 = new ServiceMetaInfo();
        service3.setServiceName("service3");
        service3.setServiceHost("localhost");
        service3.setServicePort(8082);
    }

    @Test
    public void select_withSingleService_shouldReturnSame() {
        List<ServiceMetaInfo> serviceList = Collections.singletonList(service1);

        ServiceMetaInfo result1 = loadBalancer.select(null, serviceList);
        assertEquals("应返回唯一可用的服务", service1, result1);

        ServiceMetaInfo result2 = loadBalancer.select(null, serviceList);
        assertEquals("仍然应返回唯一可用的服务", service1, result2);
    }

    @Test
    public void select_withEmptyOrNullList_shouldReturnNull() {
        List<ServiceMetaInfo> emptyList = Collections.emptyList();
        assertNull("对于空列表应返回null", loadBalancer.select(null, emptyList));

        assertNull("对于null列表应返回null", loadBalancer.select(null, null));
    }

    @Test
    public void select_shouldReturnRandomService() {
        List<ServiceMetaInfo> serviceList = Arrays.asList(service1, service2, service3);
        
        // 多次调用，结果应该在服务列表中
        for (int i = 0; i < 10; i++) {
            ServiceMetaInfo result = loadBalancer.select(null, serviceList);
            assertNotNull("返回结果不应为null", result);
            assertTrue("返回结果应在服务列表中", serviceList.contains(result));
        }
    }

    @Test
    public void select_distributionShouldBeRoughlyEven() {
        List<ServiceMetaInfo> serviceList = Arrays.asList(service1, service2, service3);
        int totalCalls = 1000;
        Map<ServiceMetaInfo, Integer> distributionMap = new HashMap<>();
        
        // 执行大量调用并统计分布
        for (int i = 0; i < totalCalls; i++) {
            ServiceMetaInfo result = loadBalancer.select(null, serviceList);
            distributionMap.put(result, distributionMap.getOrDefault(result, 0) + 1);
        }
        
        // 验证每个服务都被选择过
        assertEquals("所有服务都应该被选择过", serviceList.size(), distributionMap.size());
        
        // 验证分布相对均匀（允许一定的随机偏差）
        double expectedAverage = totalCalls / (double) serviceList.size();
        double allowedDeviation = expectedAverage * 0.3; // 允许30%的偏差
        
        for (ServiceMetaInfo service : serviceList) {
            int count = distributionMap.get(service);
            assertTrue(
                String.format("服务 %s 的选择次数 %d 应该接近期望值 %.2f (允许偏差 %.2f)", 
                    service.getServiceName(), count, expectedAverage, allowedDeviation),
                Math.abs(count - expectedAverage) <= allowedDeviation
            );
        }
    }

    @Test
    public void select_isThreadSafe() throws InterruptedException {
        List<ServiceMetaInfo> serviceList = Arrays.asList(service1, service2, service3);
        int numThreads = 10;
        int numCallsPerThread = 100;
        int totalCalls = numThreads * numCallsPerThread;

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<ServiceMetaInfo> results = Collections.synchronizedList(new java.util.ArrayList<>());
        Map<ServiceMetaInfo, AtomicInteger> countMap = new ConcurrentHashMap<>();
        
        for (ServiceMetaInfo service : serviceList) {
            countMap.put(service, new AtomicInteger(0));
        }

        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < numCallsPerThread; j++) {
                    ServiceMetaInfo selected = loadBalancer.select(null, serviceList);
                    results.add(selected);
                    countMap.get(selected).incrementAndGet();
                }
            });
        }

        executorService.shutdown();
        assertTrue("线程应及时完成", executorService.awaitTermination(1, TimeUnit.MINUTES));

        assertEquals("所有调用都应返回结果", totalCalls, results.size());

        // 验证所有返回的服务是否来自原始列表
        for(ServiceMetaInfo result : results) {
            assertNotNull(result);
            assertTrue("每个结果必须是初始服务之一", serviceList.contains(result));
        }
        
        // 验证分布相对均匀
        double expectedAverage = totalCalls / (double) serviceList.size();
        double allowedDeviation = expectedAverage * 0.3; // 允许30%的偏差
        
        for (ServiceMetaInfo service : serviceList) {
            int count = countMap.get(service).get();
            assertTrue(
                String.format("多线程环境下，服务 %s 的选择次数 %d 应该接近期望值 %.2f (允许偏差 %.2f)", 
                    service.getServiceName(), count, expectedAverage, allowedDeviation),
                Math.abs(count - expectedAverage) <= allowedDeviation
            );
        }
    }
} 