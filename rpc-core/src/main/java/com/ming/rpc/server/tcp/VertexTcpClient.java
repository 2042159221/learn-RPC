package com.ming.rpc.server.tcp;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


import com.ming.rpc.RpcApplication;
import com.ming.rpc.model.RpcRequest;
import com.ming.rpc.model.RpcResponse;
import com.ming.rpc.model.ServiceMetaInfo;
import com.ming.rpc.protocol.ProtocolConstant;
import com.ming.rpc.protocol.ProtocolMessage;
import com.ming.rpc.protocol.ProtocolMessageDecoder;
import com.ming.rpc.protocol.ProtocolMessageEncoder;
import com.ming.rpc.protocol.ProtocolMessageSerializerEnum;
import com.ming.rpc.protocol.ProtocolMessageTypeEnum;

import cn.hutool.core.util.IdUtil;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetClient;
import io.vertx.core.net.NetSocket;

/**
 * Vertx 实现的 TCP 客户端
 */
public class VertexTcpClient {
    /**
     * 发送请求
     * @param rpcRequest
     * @param serviceMetaInfo
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     */
    public static RpcResponse doRequest(RpcRequest rpcRequest,ServiceMetaInfo serviceMetaInfo) throws InterruptedException,ExecutionException{

        //发送TCP请求
        Vertx vertx = Vertx.vertx();
        NetClient netClient = vertx.createNetClient();
        CompletableFuture<RpcResponse> responseFuture = new CompletableFuture<>();
        netClient.connect(serviceMetaInfo.getServicePort(), serviceMetaInfo.getServiceHost(),
        result -> {
            if(!result.succeeded()){
                System.err.println("connect to server failed");
                return;
            }
            NetSocket socket = result.result();
            //发送数据
            //构造消息
            ProtocolMessage<RpcRequest> protocolMessage = new ProtocolMessage<>();
            ProtocolMessage.Header header = new ProtocolMessage.Header();
            header.setMagic(ProtocolConstant.PROTOCOL_MAGIC);
            header.setVersion(ProtocolConstant.PROTOCOL_VERSION);
            header.setSerializer((byte)ProtocolMessageSerializerEnum.getEnumByValue(RpcApplication.getRpcConfig().getSerializer()).getKey());
            header.setType((byte)ProtocolMessageTypeEnum.REQUEST.getKey());
            //生成全局请求ID
            header.setRequestId(IdUtil.getSnowflakeNextId());
            protocolMessage.setHeader(header);
            protocolMessage.setBody(rpcRequest);

            //编码请求
            try{
                Buffer encodeBuffer = ProtocolMessageEncoder.encode(protocolMessage);
                socket.write(encodeBuffer);
            } catch(IOException e){
                throw new RuntimeException("encode protocol message error",e);
            }

            //接受响应
            TcpBufferHandlerWrapper bufferHandlerWrapper  = new TcpBufferHandlerWrapper(
                buffer -> {
                    try{
                        ProtocolMessage<RpcResponse> rpcResponseProtocolMessage = (
                            ProtocolMessage<RpcResponse>
                         ) ProtocolMessageDecoder.decode(buffer);
                         responseFuture.complete(rpcResponseProtocolMessage.getBody());
                    }catch(IOException e){
                        throw new RuntimeException("decode protocol message error",e);
                    }
                }
            );
            socket.handler(bufferHandlerWrapper);
        });
        RpcResponse rpcResponse = responseFuture.get();
        netClient.close();
        return rpcResponse;
    }
}
