package com.ming.rpc.model.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * RPC response
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse implements Serializable{
    private Object data;
    private Class<?> dataType;
    private String message;
    private Exception exception;
    
    @Override
    public String toString() {
        return "RpcResponse{" +
                "data=" + data +
                ", dataType=" + (dataType == null ? "null" : dataType.getName()) +
                ", message='" + message + '\'' +
                ", exception=" + (exception == null ? "null" : exception.getMessage()) +
                '}';
    }
}
