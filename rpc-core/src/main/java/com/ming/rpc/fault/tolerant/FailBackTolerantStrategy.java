package com.ming.rpc.fault.tolerant;

import java.util.Map;

import com.ming.rpc.RpcApplication;
import com.ming.rpc.config.RpcConfig;
import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/** 
 * 降级到其他服务 - 容错策略
 */
@Slf4j  
public class FailBackTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        log.info("Executing FailBack tolerant strategy.", e);
        
        // 从上下文中获取 rpcRequest
        RpcRequest rpcRequest = (RpcRequest) context.get("rpcRequest");
        if (rpcRequest == null) {
            log.warn("RpcRequest not found in context, cannot perform failback.");
            return new RpcResponse();
        }

        // 从全局配置中获取降级服务
        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Class<?> mockService = rpcConfig.getMockServiceRegistry().get(rpcRequest.getServiceName());

        // 如果找到了降级服务，则通过反射调用
        if (mockService != null) {
            try {
                log.info("Found mock service for {}, executing fallback.", rpcRequest.getServiceName());
                Object mockInstance = mockService.getDeclaredConstructor().newInstance();
                Method method = mockService.getMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
                Object result = method.invoke(mockInstance, rpcRequest.getArgs());
                
                // 封装成功响应
                RpcResponse response = new RpcResponse();
                response.setData(result);
                response.setMessage("Fallback success");
                return response;
            } catch (Exception reflectionException) {
                log.error("Failed to execute mock service via reflection.", reflectionException);
            }
        }
        
        // 如果没有找到降级服务或反射失败，则返回一个空的响应
        log.warn("No mock service found for {}, returning empty response.", rpcRequest.getServiceName());
        return new RpcResponse();
    }

}
