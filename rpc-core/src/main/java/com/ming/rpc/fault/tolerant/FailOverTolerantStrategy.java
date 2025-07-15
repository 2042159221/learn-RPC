package com.ming.rpc.fault.tolerant;

import com.ming.rpc.loadbalancer.LoadBalancer;
import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
import com.ming.rpc.model.ServiceMetaInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.HashMap;

/**
 * 失败切换到其他服务 - 容错策略
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        // 从上下文中获取所需参数
        List<ServiceMetaInfo> serviceNodeList = (List<ServiceMetaInfo>) context.get("serviceNodeList");
        ServiceMetaInfo failedNode = (ServiceMetaInfo) context.get("selectedNode");
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");
        LoadBalancer loadBalancer = (LoadBalancer) context.get("loadBalancer");
        // 获取重试器，这是执行RPC调用的核心逻辑
        Retryer retryer = (Retryer) context.get("retryer");
        // 获取已重试次数
        AtomicInteger retriedCount = (AtomicInteger) context.get("retriedCount");

        // 从可用节点列表中移除已失败的节点
        List<ServiceMetaInfo> availableNodes = serviceNodeList.stream()
                .filter(node -> !node.equals(failedNode))
                .collect(Collectors.toList());
        
        // 检查是否还有可用节点
        if (availableNodes.isEmpty()) {
            log.error("All service nodes failed, no node available for failover.", e);
            throw new RuntimeException("All service nodes failed.", e);
        }
        
        // 构建负载均衡器所需的请求参数Map
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", rpcRequest.getMethodName());
        
        // 从剩余节点中重新选择一个
        ServiceMetaInfo nextNode = loadBalancer.select(requestParams, availableNodes);

        log.info("Failing over to new node: {}", nextNode.getServiceAddress());
        
        // 增加重试次数
        retriedCount.incrementAndGet();

        // 使用重试器执行调用
        return retryer.doRetry(rpcRequest, nextNode);
    }

    /**
     * 定义一个函数式接口，用于封装重试逻辑
     */
    @FunctionalInterface
    public interface Retryer {
        RpcResponse doRetry(RpcRequest rpcRequest, ServiceMetaInfo serviceMetaInfo);
    }
}
