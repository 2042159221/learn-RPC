package com.ming.rpc.protocol;

import java.io.IOException;

import com.ming.rpc.serializer.Serializer;
import com.ming.rpc.serializer.SerializerFactory;

import io.vertx.core.buffer.Buffer;

/**
 * 协议消息编码器
 * 
 */
public class ProtocolMessageEncoder {

    /**
     * 编码
     * @param protocolMessage 协议消息
     * @return 编码后的字节数组
     * @throws IOException 编码异常
     */
    public static Buffer encode(ProtocolMessage<?> protocolMessage) throws IOException {
        if (protocolMessage == null || protocolMessage.getHeader() == null) {
            return Buffer.buffer();
        }
        ProtocolMessage.Header header = protocolMessage.getHeader();
        //依次向缓冲区写入字节
        Buffer buffer = Buffer.buffer();
        buffer.appendByte(header.getMagic());
        buffer.appendByte(header.getVersion());
        buffer.appendByte(header.getSerializer());
        buffer.appendByte(header.getType());
        buffer.appendByte(header.getStatus());
        buffer.appendLong(header.getRequestId());
        //获取序列器
        ProtocolMessageSerializerEnum serializerEnum = ProtocolMessageSerializerEnum.getEnumByKey(header.getSerializer());
        if (serializerEnum == null) {
            throw new IOException("不支持的序列器类型：" + header.getSerializer());
        }
        Serializer serializer = SerializerFactory.getInstance(serializerEnum.getValue());

        //序列化消息体
        byte[] bodyBytes = serializer.serialize(protocolMessage.getBody());
        //写入消息体长度
        buffer.appendInt(bodyBytes.length);
        //写入消息体
        buffer.appendBytes(bodyBytes);
        return buffer;
    }
}
