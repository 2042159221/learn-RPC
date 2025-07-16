package com.ming.rpc.server.tcp;

import com.ming.rpc.protocol.ProtocolConstant;
import io.vertx.core.buffer.Buffer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TcpBufferHandlerWrapper测试类
 * 专门测试半包粘包处理功能
 */
public class TcpBufferHandlerWrapperTest {

    /**
     * 测试半包粘包处理
     */
    @Test
    @DisplayName("测试TCP半包粘包处理")
    public void testTcpBufferHandlerWrapper() {
        System.out.println("开始执行TcpBufferHandlerWrapper测试...");
        
        // 创建结果引用
        AtomicReference<Buffer> resultBuffer = new AtomicReference<>();
        CompletableFuture<Buffer> future = new CompletableFuture<>();
        
        // 创建缓冲区处理包装器
        TcpBufferHandlerWrapper wrapper = new TcpBufferHandlerWrapper(buffer -> {
            System.out.println("接收到处理完成的buffer，长度: " + buffer.length());
            resultBuffer.set(buffer);
            future.complete(buffer);
        });
        
        // 模拟消息头（假设消息头长度为17字节，第13位开始是body长度信息）
        Buffer headerBuffer = Buffer.buffer();
        for (int i = 0; i < 13; i++) {
            headerBuffer.appendByte((byte) i);
        }
        // 设置body长度为10
        headerBuffer.appendInt(10);
        System.out.println("创建测试消息头，长度: " + headerBuffer.length() + ", 消息体长度设置为: 10");
        
        // 模拟消息体
        Buffer bodyBuffer = Buffer.buffer();
        for (int i = 0; i < 10; i++) {
            bodyBuffer.appendByte((byte) i);
        }
        System.out.println("创建测试消息体，长度: " + bodyBuffer.length());
        
        // 发送消息头
        System.out.println("发送消息头...");
        wrapper.handle(headerBuffer);
        
        // 发送消息体
        System.out.println("发送消息体...");
        wrapper.handle(bodyBuffer);
        
        try {
            // 等待处理完成
            System.out.println("等待处理完成...");
            Buffer result = future.get(5, TimeUnit.SECONDS);
            
            // 验证结果
            System.out.println("接收到最终结果，长度: " + result.length());
            assertNotNull(result, "结果不能为空");
            
            // 总长度应该是头部(17) + 消息体(10)
            int expectedLength = ProtocolConstant.MESSAGE_HEADER_LENGTH + 10;
            System.out.println("预期长度: " + expectedLength + ", 实际长度: " + result.length());
            
            assertEquals(expectedLength, result.length(), 
                "缓冲区长度应为" + expectedLength + "(头部长度" + ProtocolConstant.MESSAGE_HEADER_LENGTH + "+消息体长度10)");
            
            System.out.println("测试通过！");
        } catch (Exception e) {
            System.err.println("测试失败: " + e.getMessage());
            e.printStackTrace();
            fail("测试异常：" + e.getMessage());
        }
    }
} 