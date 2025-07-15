package com.ming.rpc.fault.retry;

import java.util.concurrent.Callable;

import com.ming.rpc.model.RpcResponse;

/**
 * 重试策略
 */
public interface RetryStrategy {
    /**
     * 重试
     * @param callable 可调用对象
     * @return 返回结果
     * @throws Exception 异常
     */
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
