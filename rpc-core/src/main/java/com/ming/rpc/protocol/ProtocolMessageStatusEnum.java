package com.ming.rpc.protocol;
import lombok.Getter;

/**
 * 协议消息状态枚举
 */
@Getter
public enum ProtocolMessageStatusEnum {
    OK("OK",20),
    BAD_REQUEST("bad_request",40),
    BAD_RESPONSE("bad_response",50);

    private final String text;
    private final int value;

    ProtocolMessageStatusEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }


    /**
     * 根据value获取枚举
     * @param value
     * @return
     */
    public static ProtocolMessageStatusEnum getEnumByValue(int value) {
        for (ProtocolMessageStatusEnum protocolMessageStatusEnum : ProtocolMessageStatusEnum.values()) {
            if (protocolMessageStatusEnum.getValue() == value) {
                return protocolMessageStatusEnum;
            }
        }
        return null;
    }
}
