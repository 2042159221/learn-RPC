package com.ming.rpc.server.http;

import io.vertx.core.Vertx;


/**
 * Vertx HTTP 服务器（启动类） 
 */
public class VertxHttpServer implements HttpServer{
     

    
    /**
     * 启动服务器
     * @param port
     */
    public void doStart(int port){
       //创建Vertx实例
       Vertx vertx = Vertx.vertx();

       //创建HTTP服务器
       io.vertx.core.http.HttpServer server = vertx.createHttpServer();

       //处理请求
       server.requestHandler(new HttpServerHandler());

       // 启动HTTP服务器并监听端口
       server.listen(port, "0.0.0.0", result -> {
        if (result.succeeded()) {
            System.out.println("Vertx HTTP Server started on port " + port);
        } else {
            System.err.println("Failed to start Vertx HTTP Server on port " + result.cause().getMessage());
        }
       });  
    }

}
