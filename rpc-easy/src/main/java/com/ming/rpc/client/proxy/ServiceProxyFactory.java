package com.ming.rpc.client.proxy;

import com.ming.rpc.client.transport.HttpClient;
import com.ming.rpc.model.request.RpcRequest;
import com.ming.rpc.model.response.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * 服务代理工厂
 * 用于创建服务接口的代理实现
 */
public class ServiceProxyFactory {
    


    /**
     * 创建服务代理（使用默认主机名和端口）
     * @param serviceClass 服务接口类
     * @param <T> 服务接口类型
     * @return 服务代理对象
     */
    public static <T> T getProxy(Class<T> serviceClass) {
        return (T) Proxy.newProxyInstance(
            serviceClass.getClassLoader(),
            new Class[]{serviceClass},
            new ServiceProxy());
    }

    /**
     * 创建服务代理
     * @param serviceClass 服务接口类
     * @param host 服务器主机名
     * @param port 服务器端口
     * @param <T> 服务接口类型
     * @return 服务代理对象
     */
    public static <T> T getProxy(Class<T> serviceClass, String host, int port) {
        // 创建HTTP客户端
        HttpClient httpClient = new HttpClient();
        
        // 创建动态代理
        return (T) Proxy.newProxyInstance(
                serviceClass.getClassLoader(),
                new Class[]{serviceClass},
                new ServiceInvocationHandler(httpClient, serviceClass, host, port)
        );
    }

    /**
     * 服务调用处理器
     * 实现InvocationHandler接口，处理代理对象的方法调用
     */
    private static class ServiceInvocationHandler implements InvocationHandler {
        private final HttpClient httpClient;
        private final Class<?> serviceClass;
        private final String host;
        private final int port;

        public ServiceInvocationHandler(HttpClient httpClient, Class<?> serviceClass, String host, int port) {
            this.httpClient = httpClient;
            this.serviceClass = serviceClass;
            this.host = host;
            this.port = port;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 处理Object类的方法
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            
            // 构建RPC请求
            RpcRequest rpcRequest = RpcRequest.builder()
                    .serviceName(serviceClass.getName())
                    .methodName(method.getName())
                    .parameterTypes(method.getParameterTypes())
                    .args(args)
                    .build();
            
            // 发送请求并获取响应
            RpcResponse rpcResponse = httpClient.sendRequest(rpcRequest, host, port);
            
            // 处理异常
            if (rpcResponse.getException() != null) {
                throw rpcResponse.getException();
            }
            
            // 返回结果
            return rpcResponse.getData();
        }
    }
} 