package com.ming.rpc.registry;

import java.util.List;

import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;

/**
 * 注册中心
 */
public interface Registry {
    /**
     * 初始化
     * @param registryConfig 注册中心配置
     */
    void init(RegistryConfig registryConfig);

    /**
     * 注册服务，服务端
     * @param serviceMetaInfo 服务元信息
     */
    void register(ServiceMetaInfo serviceMetaInfo) throws Exception;

    /**
     * 注销服务，服务端
     * @param serviceMetaInfo 服务元信息
     */
    void unregister(ServiceMetaInfo serviceMetaInfo);

    /**
     * 服务发现（获取某服务所有节点，消费端）
     * @param serviceKey 服务键名
     * @return 
     */
    List<ServiceMetaInfo> serviceDiscovery(String serviceKey);

    /**
     * 心跳检测 （ 服务端）
     */
    void heartbeat();

     /**
      * 监听（消费端)
      * @param serviceNodeKey
      */
      void watch(String serviceNodeKey);

      /**
       * 服务销毁
       */
      void destroy();
}
