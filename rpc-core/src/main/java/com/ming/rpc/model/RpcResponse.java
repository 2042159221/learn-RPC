package com.ming.rpc.model;

import java.io.Serializable;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * RPC 响应
 * 
 * 
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable{

    /**
     * 响应数据
     */
    private Object data;

    /**
     * 响应数据类型（预留）
     * 
     */
    private Class<?> dataType;

    /**
     * 响应信息
     * 
     */
    private String message;

    /**
     * 异常信息
     */
    private Exception exception;

}
