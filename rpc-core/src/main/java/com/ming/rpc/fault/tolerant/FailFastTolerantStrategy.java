package com.ming.rpc.fault.tolerant;

import java.util.Map;

import com.ming.rpc.model.RpcResponse;
/**
 * 快速失败容错策略（立刻通知外层调用方）
 */
public class FailFastTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        throw new RuntimeException("服务报错",e);
    }
}
