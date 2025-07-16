package com.ming.rpc.bootstrap;

import java.util.List;

import com.ming.rpc.RpcApplication;
import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.config.RpcConfig;
import com.ming.rpc.model.ServiceMetaInfo;
import com.ming.rpc.model.ServiceRegisterInfo;
import com.ming.rpc.registry.LocalRegistry;
import com.ming.rpc.registry.Registry;
import com.ming.rpc.registry.RegistryFactory;
import com.ming.rpc.server.tcp.VertexTcpServer;

/**
 * 服务提供者启动类（初始化）
 */
public class ProviderBootstrap {
    /**
     * 初始化
     */
    public static void init(List<ServiceRegisterInfo<?>> serviceRegisterInfoList){
    // RPC 框架初始化 （配置和注册中心）
    RpcApplication.init();
    //全局配置
    final RpcConfig rpcConfig = RpcApplication.getRpcConfig();

        //注册服务
        for (ServiceRegisterInfo<?> serviceRegisterInfo : serviceRegisterInfoList) {
            String serviceName = serviceRegisterInfo.getServiceName();
            //本地注册
            LocalRegistry.register(serviceName, serviceRegisterInfo.getImplClass());

            //注册中心注册
            RegistryConfig registryConfig = rpcConfig.getRegistryConfig();
            Registry registry = RegistryFactory.getInstance(registryConfig.getRegistry());
            ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();

            serviceMetaInfo.setServiceName(serviceName);
            serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
            serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
            try{
                registry.register(serviceMetaInfo);
            }catch(Exception e){
                e.printStackTrace();
                throw new RuntimeException("服务注册失败", e);
            }
        }
        //启动服务器
        VertexTcpServer vertexTcpServer = new VertexTcpServer();
        vertexTcpServer.doStart(rpcConfig.getServerPort());
    }
}
