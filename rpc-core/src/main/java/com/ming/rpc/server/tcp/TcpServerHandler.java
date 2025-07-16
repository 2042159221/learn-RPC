package com.ming.rpc.server.tcp;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;

import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
import com.ming.rpc.protocol.ProtocolMessage;
import com.ming.rpc.protocol.ProtocolMessageDecoder;
import com.ming.rpc.protocol.ProtocolMessageEncoder;
import com.ming.rpc.protocol.ProtocolMessageStatusEnum;
import com.ming.rpc.protocol.ProtocolMessageTypeEnum;
import com.ming.rpc.registry.LocalRegistry;


/**
 * TCP 请求处理器
 */
public class TcpServerHandler implements Handler<NetSocket> {
    /**
     * 处理 TCP 请求
     * @param socket
     */

    @Override
    public void handle(NetSocket socket) {
        TcpBufferHandlerWrapper bufferHandlerWrapper = new  TcpBufferHandlerWrapper(buffer -> {
            // 处理 TCP 请求,decode 解码
            ProtocolMessage<RpcRequest> protocolMessage ;
            try{
                protocolMessage = ( ProtocolMessage<RpcRequest>) ProtocolMessageDecoder.decode(buffer);
            }catch(Exception e){
               throw new RuntimeException("decode protocol message error",e);
            }
            // 处理 RPC 请求
            RpcRequest rpcRequest = protocolMessage.getBody();
            ProtocolMessage.Header header = protocolMessage.getHeader();

            RpcResponse rpcResponse = new RpcResponse();
            try{
                //获取要调用的服务实现类，通过反射调用
                Class<?> impClass = LocalRegistry.get(rpcRequest.getServiceName()) ;
                Method method =impClass.getMethod(rpcRequest.getMethodName(),rpcRequest.getParameterTypes());
                Object result = method.invoke(impClass.newInstance(),rpcRequest.getArgs());
                //封装返回结果
                rpcResponse.setData(result);
                rpcResponse.setDataType(method.getReturnType());
                rpcResponse.setMessage("ok");
            }catch(Exception e){
               e.printStackTrace();
               rpcResponse.setMessage(e.getMessage());
               rpcResponse.setException(e);
            }

            // 编码 RPC 响应
            header.setType((byte)ProtocolMessageTypeEnum.RESPONSE.getKey());
            header.setStatus((byte)ProtocolMessageStatusEnum.OK.getValue());
            ProtocolMessage<RpcResponse> responseProtocolMessage = new ProtocolMessage<>(header,rpcResponse);
            try{
                Buffer encode = ProtocolMessageEncoder.encode(responseProtocolMessage);
                socket.write(encode);
            }catch (IOException e){
                throw new RuntimeException("encode protocol message error",e);
            }
        });
        socket.handler(bufferHandlerWrapper);
    }

}
