package com.ming.rpc.registry;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
    /**
     * LocalRegistry 本地注册中心
     */
public class LocalRegistry {

    private static final Map<String,Class<?>> map = new ConcurrentHashMap<>();

    /**
     * 注册服务
     * @param serviceName 服务名称
     * @param implClass 服务类
     */
    public static void register(String serviceName,Class<?> implClass){
        System.out.println("注册服务: " + serviceName + " -> " + implClass.getName());
        map.put(serviceName,implClass);
    }
    /**
     * Get service 
     * @param serviceName 服务名称
     * @return 服务类
     */
    public static Class<?> get(String serviceName){
        Class<?> implClass = map.get(serviceName);
        if (implClass == null) {
            System.out.println("未找到服务: " + serviceName);
        }
        return implClass;
    }

    /**
     * delete service
     * @param serviceName 服务名称
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
