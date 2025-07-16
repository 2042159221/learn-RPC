package com.ming.rpc.exception;

/**
 * 自定义异常类
 *
 * @author ming
 */
public class RpcException extends RuntimeException {

    public RpcException(String message) {
        super(message);
    }

}
