package com.ming.rpc.model.request;

import java.io.Serializable;
import java.util.Arrays;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * RPC request
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable{
    private String serviceName;
    private String methodName;
    private Object[] args;
    private Class<?>[] parameterTypes;
    
    @Override
    public String toString() {
        return "RpcRequest{" +
                "serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", args=" + (args == null ? "null" : Arrays.toString(args)) +
                ", parameterTypes=" + (parameterTypes == null ? "null" : Arrays.toString(parameterTypes)) +
                '}';
    }
}
