package com.ming.rpc.server;
import com.ming.rpc.server.handler.HttpServerHandler;

import io.vertx.core.Vertx;

public class VertexHttpServer implements HttpServer {

    @Override
    public void doStart(int port) {
        //创建Vertx.x实例
       Vertx vertx = Vertx.vertx();
       //创建HttpServer实例
       io.vertx.core.http.HttpServer server = vertx.createHttpServer();
       // 监听端口并处理请求
       server.requestHandler(new HttpServerHandler());
       //启动HTTP服务器并监听指定端口
       server.listen(port,result ->{
        if(result.succeeded()){
            System.out.println("Server is now listening on port " + port);
        }else{
            System.out.println("Failed to start server o " + result.cause());
        }
       });
    }

}
