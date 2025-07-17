package com.ming.rpc.registry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 本地注册中心
 */
public class LocalRegistry {

    /**
     * 注册信息存储
     */
    private static final Map<String,Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     * @param serviceName
     * @param implClass
     *
     */
    public  static void register(String serviceName,Class<?> implClass){
        map.put(serviceName, implClass);
    }

    /**
     * 获取服务实现类
     * @param serviceName
     * @return
     */
    public static Class<?> get(String serviceName){
        return map.get(serviceName);
    }

    /**
     * 注销服务
     * @param serviceName
     */
    public static void remove(String serviceName){
        map.remove(serviceName);
    }

    /**
     * 列出所有已注册的服务
     * @return 服务名称集合
     */
    public static Set<String> listServices() {
        return map.keySet();
    }
}
