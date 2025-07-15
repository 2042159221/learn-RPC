package com.ming.rpc.fault.tolerant;

import java.util.Map;

import com.ming.rpc.model.RpcResponse;

/**
 * 容错策略
 */
public interface TolerantStrategy {
    
    /**
     * 容错
     * @param context 上下文
     * @param e 异常
     * @return
     */     
    RpcResponse doTolerant(Map<String, Object> context, Exception e);
    
}
