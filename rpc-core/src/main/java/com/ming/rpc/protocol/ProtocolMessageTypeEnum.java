package com.ming.rpc.protocol;

import org.apache.zookeeper.server.FinalRequestProcessor;

import lombok.Getter;

/**
 * 协议消息的类型枚举
 */
@Getter
public enum ProtocolMessageTypeEnum {

    REQUEST(0),
    RESPONSE(1),
    HEART_BEAT(2),
    OTHERS(3);

    private final int key;

    ProtocolMessageTypeEnum(int key) {
        this.key = key;
    }

    /**
     * 根据类型获取枚举
     * @param type
     * @return
     */
    public static ProtocolMessageTypeEnum getEnumByType(int type) {
        for (ProtocolMessageTypeEnum protocolMessageTypeEnum : ProtocolMessageTypeEnum.values()) {
            if (protocolMessageTypeEnum.getKey() == type) {
                return protocolMessageTypeEnum;
            }
        }
        return null;
    }
}