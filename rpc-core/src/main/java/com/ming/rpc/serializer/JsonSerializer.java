package com.ming.rpc.serializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;

import java.io.IOException;

/**
 * Json 序列化器
 */
public class JsonSerializer implements Serializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
/**
 * ServiceMetaInfo 类中的 getServiceKey()、getServiceNodeKey() 和 getServiceAddress() 方法没有对应字段，导致 Jackson 序列化时产生问题，并在反序列化时抛出 UnrecognizedPropertyException
 * 配置 ObjectMapper 忽略未知属性
 */
    static {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    @Override
    public byte[] serialize(Object obj) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> classType) throws IOException {
        T  obj = OBJECT_MAPPER.readValue(bytes , classType);
        if(obj instanceof RpcRequest) {
            return handleReqest((RpcRequest) obj, classType);
        }
        if(obj instanceof RpcResponse) {
            return handleResponse((RpcResponse) obj, classType);
        }

        return obj;
    }


    /**
     * 由于Object 的原始对昂会被擦除，熬制反序列化时会被作为 LinkedHashMap 无法转换成原始对象，因此这里做了特殊处理
     * @param rpcRequest rpc 请求
     * @param type 类类型
     * @return {@link T}
     * @throws IOException  IO异常
     */
    private <T> T handleReqest(RpcRequest rpcRequest, Class<T> type) throws IOException {
        Class<?>[] parameterTypes = rpcRequest.getParameterTypes();
        Object[] args = rpcRequest.getArgs();

        // 循环处理每个参数的类型
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> clazz = parameterTypes[i];
            //如果类型不同，则重新处理一下类型
            if (!clazz.isAssignableFrom(args[i].getClass())) {
                byte[] argBytes = OBJECT_MAPPER.writeValueAsBytes(args[i]);
                args[i] = OBJECT_MAPPER.readValue(argBytes, clazz);
            }
        }
        return type.cast(rpcRequest);
    }

    /**
     *
     * @param rpcResponse rpc 响应
     * @param type 类类型
     * @return {@link T}
     * @throws IOException  IO异常
     */
    private <T> T handleResponse(RpcResponse rpcResponse, Class<T> type) throws IOException {
        byte[] dataBytes = OBJECT_MAPPER.writeValueAsBytes(rpcResponse.getData());
        rpcResponse.setData(OBJECT_MAPPER.readValue(dataBytes, rpcResponse.getDataType()));
        return type.cast(rpcResponse);
    }
}






