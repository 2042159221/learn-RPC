package com.ming.rpc.server.tcp;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetServer;
import lombok.extern.slf4j.Slf4j;

/**
 * Vert.x TCP 服务端
 */
@Slf4j
public class VertexTcpServer {
    public void doStart(int port) {
        //创建 Vert.x 实例
        Vertx vertx = Vertx.vertx();
        //创建 TCP 服务端
        NetServer netServer = vertx.createNetServer();
        //设置 TCP 服务端监听端口
        netServer.connectHandler(new TcpServerHandler());
        //启动 TCP 服务端
        netServer.listen(port, ar -> {
            if (ar.succeeded()) {
                log.info("TCP 服务端启动成功，监听端口：{}", port);
            } else {
                log.error("TCP 服务端启动失败，监听端口：{}", port, ar.cause());
            }
        });
    }
    public static void main(String[] args) {
       new VertexTcpServer().doStart(8888);
    }
}
