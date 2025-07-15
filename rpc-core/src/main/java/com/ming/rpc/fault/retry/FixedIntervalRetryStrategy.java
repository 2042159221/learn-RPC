package com.ming.rpc.fault.retry;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.rholder.retry.*;
import com.ming.rpc.model.RpcResponse;

/**
 * 固定时间间隔重试 - 重试策略实现
 */
public class FixedIntervalRetryStrategy implements RetryStrategy {

    /**
     * 重试
     * @param callable 可调用对象
     * @return 返回结果
     * @throws ExecutionException 异常
     * @throws RetryException 重试异常
     */
    @Override
    public RpcResponse doRetry(Callable<RpcResponse> callable) throws ExecutionException,RetryException {
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
            .retryIfExceptionOfType(Exception.class)
            .withWaitStrategy(WaitStrategies.fixedWait(3L, TimeUnit.SECONDS))
            .withStopStrategy(StopStrategies.stopAfterAttempt(3))
            .withRetryListener(new RetryListener() {
                @Override
                public <V> void onRetry(Attempt<V> attempt) {
                    // 增加hasException判断，避免成功时调用getExceptionCause抛出异常
                    if (attempt.hasException()) {
                        System.out.println("重试次数：" + attempt.getAttemptNumber() + "，异常：" + attempt.getExceptionCause());
                    }
                }
            })
            .build();
        return retryer.call(callable);
    }
}
