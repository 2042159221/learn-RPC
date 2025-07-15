package com.ming.rpc.protocol;


import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

/**
 * 协议消息序列化枚举
 */
@Getter
public enum ProtocolMessageSerializerEnum {
    JDK(0,"jdk"),
    JSON(1,"json"),
    KRYO(2,"kryo"),
    HESSIAN(3,"hessian");

    private final int key;
    private final String value;

    ProtocolMessageSerializerEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * 根据key获取枚举
     * @param key
     * @return
     */
    public static ProtocolMessageSerializerEnum getEnumByKey(int key) {
        for (ProtocolMessageSerializerEnum protocolMessageSerializerEnum : ProtocolMessageSerializerEnum.values()) {
            if (protocolMessageSerializerEnum.getKey() == key) {
                return protocolMessageSerializerEnum;
            }
        }
        return null;
    }

    /**
     * 根据value获取枚举
     */
    public static ProtocolMessageSerializerEnum getEnumByValue(String value) {
        if(ObjectUtil.isEmpty(value)){
            return null;
        }
        for (ProtocolMessageSerializerEnum protocolMessageSerializerEnum : ProtocolMessageSerializerEnum.values()) {
            if (protocolMessageSerializerEnum.getValue().equals(value)) {
                return protocolMessageSerializerEnum;
            }
        }
        return null;
    }
}
