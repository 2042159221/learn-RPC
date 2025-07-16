package com.ming.rpc.server.tcp;
import com.ming.rpc.protocol.ProtocolConstant;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;

/**
 * TCP 消息处理器包装
 * 装饰者模式，使用 recordingParser 对原有的Buffer 处理能力进行增强
 */
public class TcpBufferHandlerWrapper implements Handler<Buffer> {

    /**
     * 解析器，用于解决半包、粘包问题
     */
    private final RecordParser recordParser;

    public TcpBufferHandlerWrapper(Handler<Buffer> bufferHandler) {
        recordParser = initRecordParser(bufferHandler);
    }

    @Override
    public void handle(Buffer buffer) {
        recordParser.handle(buffer);
    }

    /**
     * 初始化解析器
     * @param bufferHandler
     * @return
     */
    private RecordParser initRecordParser(Handler<Buffer> bufferHandler) {
        //构造 parser
        RecordParser parser = RecordParser.newFixed(ProtocolConstant.MESSAGE_HEADER_LENGTH);

        parser.setOutput(new Handler<Buffer>() {
            //标记当前是否正在处理消息头
            boolean isReadingHeader = true;
            //一次完整的读取（头+体）
            Buffer resultBuffer = Buffer.buffer();

            @Override
            public void handle(Buffer buffer) {
                if (isReadingHeader) {
                    // 读取消息头
                    resultBuffer.appendBuffer(buffer);
                    // 读取消息体长度
                    int bodyLength = buffer.getInt(13);
                    if (bodyLength > 0) {
                        // 切换到读取消息体模式
                        parser.fixedSizeMode(bodyLength);
                        isReadingHeader = false;
                    } else {
                        // 如果没有消息体，直接处理完整消息
                        bufferHandler.handle(resultBuffer);
                        // 重置，准备读取下一个消息
                        resetParser();
                    }
                } else {
                    // 读取消息体
                    resultBuffer.appendBuffer(buffer);
                    // 已拼接为完整Buffer，执行处理
                    bufferHandler.handle(resultBuffer);
                    // 重置，准备读取下一个消息
                    resetParser();
                }
            }
            
            // 重置解析器状态
            private void resetParser() {
                parser.fixedSizeMode(ProtocolConstant.MESSAGE_HEADER_LENGTH);
                isReadingHeader = true;
                resultBuffer = Buffer.buffer();
            }
        });
        return parser;
    }
}
