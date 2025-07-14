package com.ming.rpc.loadbalancer;

import com.ming.rpc.model.ServiceMetaInfo;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 轮询负载均衡器测试
 */
public class RoundRobinLoadBalancerTest {

    private final RoundRobinLoadBalancer loadBalancer = new RoundRobinLoadBalancer();

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
    public void select_shouldRoundRobinCorrectly() {
        List<ServiceMetaInfo> serviceList = Arrays.asList(service1, service2);

        ServiceMetaInfo result1 = loadBalancer.select(null, serviceList);
        assertEquals("第一次调用应返回第一个服务", service1, result1);

        ServiceMetaInfo result2 = loadBalancer.select(null, serviceList);
        assertEquals("第二次调用应返回第二个服务", service2, result2);

        ServiceMetaInfo result3 = loadBalancer.select(null, serviceList);
        assertEquals("第三次调用应返回第一个服务", service1, result3);
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
    public void select_isThreadSafe() throws InterruptedException {
        List<ServiceMetaInfo> serviceList = Arrays.asList(service1, service2, service3);
        int numThreads = 10;
        int numCallsPerThread = 100;
        int totalCalls = numThreads * numCallsPerThread;

        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<ServiceMetaInfo> results = Collections.synchronizedList(new java.util.ArrayList<>());

        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                for (int j = 0; j < numCallsPerThread; j++) {
                    results.add(loadBalancer.select(null, serviceList));
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
    }
} 