package com.ming.rpc.protocol;

import lombok.Data;
/**
 * 协议消息结构
 */
@Data
public class ProtocolMessage<T> {

    /**
     * 消息头
     */
    private Header header;

    /**
     * 消息体
     */
    private T body;

    /**
     * 协议消息头
     * 
     */
    @Data
    public static class Header {
        /**
         * 魔数，保证安全
         */
        private byte magic;

        /**
         * 协议版本号
         */
        private byte version;
        
        /**
         * 序列化器
         */
        private byte serializer;

        /**
         * 消息类型（请求/响应）
         */
        private byte type;

        /**
         * 状态
         */
        private byte status;
        
        /**
         * 请求 id
         */
        private long requestId;

        /**
         * 消息长度
         */
        private int bodyLength;
        
    }

}
