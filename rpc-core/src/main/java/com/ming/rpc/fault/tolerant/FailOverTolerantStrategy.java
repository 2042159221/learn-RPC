package com.ming.rpc.fault.tolerant;

import java.util.Map;

import com.ming.rpc.model.RpcResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 失败切换到其他服务 - 容错策略
 */
@Slf4j
public class FailOverTolerantStrategy implements TolerantStrategy {

    @Override
    public RpcResponse doTolerant(Map<String, Object> context, Exception e) {
        //TODO: 可自行拓展，获取其他服务并调用
        return null;
    }
}
