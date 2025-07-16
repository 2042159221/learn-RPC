package com.ming.rpc.proxy;

import java.lang.reflect.Proxy;

import com.ming.rpc.RpcApplication;

/**
 * 服务代理工厂 （工程模式，用于创建代理对象）
 * @author ming
 */
public class ServiceProxyFactory {

    /**
     * 获取服务代理对象
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getProxy(Class<T> serviceClass) {
      if (RpcApplication.getRpcConfig().isMock()) {
        return getMockProxy(serviceClass);
      }

      return (T) Proxy.newProxyInstance(
        serviceClass.getClassLoader(),
        new Class[] { serviceClass },
        new ServiceProxy()
      );
    }
    /**
     * 根据服务类获取 Mock 代理对象
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T getMockProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(serviceClass.getClassLoader(), new Class[] { serviceClass }, new MockServiceProxy());
    }

}
