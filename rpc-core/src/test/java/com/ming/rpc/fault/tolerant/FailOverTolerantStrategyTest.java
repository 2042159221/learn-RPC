package com.ming.rpc.fault.tolerant;

import com.ming.rpc.loadbalancer.LoadBalancer;
import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
import com.ming.rpc.model.ServiceMetaInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 故障转移策略测试
 */
class FailOverTolerantStrategyTest {

    private FailOverTolerantStrategy failOverStrategy;
    private Map<String, Object> context;
    private RpcRequest rpcRequest;
    private LoadBalancer loadBalancer;
    private FailOverTolerantStrategy.Retryer retryer;

    @BeforeEach
    void setUp() {
        failOverStrategy = new FailOverTolerantStrategy();
        context = new HashMap<>();
        rpcRequest = new RpcRequest();
        rpcRequest.setServiceName("testService");
        rpcRequest.setMethodName("testMethod");

        loadBalancer = mock(LoadBalancer.class);
        retryer = mock(FailOverTolerantStrategy.Retryer.class);

        context.put("rpcRequest", rpcRequest);
        context.put("loadBalancer", loadBalancer);
        context.put("retryer", retryer);
        context.put("retriedCount", new AtomicInteger(0));
    }

    @Test
    void testDoTolerant_SuccessOnFirstRetry() {
        // 安排
        ServiceMetaInfo node1 = new ServiceMetaInfo();
        node1.setServiceHost("host1");
        ServiceMetaInfo node2 = new ServiceMetaInfo();
        node2.setServiceHost("host2");

        List<ServiceMetaInfo> serviceNodeList = Arrays.asList(node1, node2);
        context.put("serviceNodeList", serviceNodeList);
        context.put("selectedNode", node1); // 初始失败节点

        // 模拟负载均衡器选择 node2
        when(loadBalancer.select(any(), eq(Arrays.asList(node2)))).thenReturn(node2);

        // 模拟重试器调用 node2 成功
        RpcResponse successResponse = new RpcResponse();
        successResponse.setMessage("Success");
        when(retryer.doRetry(rpcRequest, node2)).thenReturn(successResponse);

        // 行动
        RpcResponse response = failOverStrategy.doTolerant(context, new RuntimeException("Initial failure"));

        // 断言
        Assertions.assertNotNull(response);
        Assertions.assertEquals("Success", response.getMessage());
        Mockito.verify(loadBalancer, Mockito.times(1)).select(any(), any());
        Mockito.verify(retryer, Mockito.times(1)).doRetry(rpcRequest, node2);
    }

    @Test
    void testDoTolerant_AllNodesFail() {
        // 安排
        ServiceMetaInfo node1 = new ServiceMetaInfo();
        node1.setServiceHost("host1");

        List<ServiceMetaInfo> serviceNodeList = List.of(node1);
        context.put("serviceNodeList", serviceNodeList);
        context.put("selectedNode", node1); // 初始失败节点

        // 行动 & 断言
        Assertions.assertThrows(RuntimeException.class, () -> {
            failOverStrategy.doTolerant(context, new RuntimeException("Initial failure"));
        });
        
        // 验证负载均衡器和重试器均未被调用
        Mockito.verify(loadBalancer, Mockito.never()).select(any(), any());
        Mockito.verify(retryer, Mockito.never()).doRetry(any(), any());
    }
} 