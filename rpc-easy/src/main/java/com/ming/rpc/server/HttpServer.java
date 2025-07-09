package com.ming.rpc.server;

/**
 * HTTP 服务器接口，定义了HTTP服务的基本操作
 */
public interface HttpServer {
    /**
     * 启动服务器
     * @param port 服务器端口
     */
    void doStart(int port);

}
