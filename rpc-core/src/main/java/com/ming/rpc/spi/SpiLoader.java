package com.ming.rpc.spi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

import com.ming.rpc.serializer.Serializer;

import cn.hutool.core.io.resource.ResourceUtil;

/**
 * SPI加载器
 * 自定义实现SPI加载器，支持键值对映射
 */
@Slf4j
public class SpiLoader {
    /**
     * 存储已加载的类：接口名 =>(key => 实现类)
     */
    private static final Map<String,Map<String,Class<?>>> loaderMap = new ConcurrentHashMap<>();

    /**
     * 对象实例缓存（避免重复 new） ，类路径 => 对象实例，单例模式
     */
    private static final Map<String,Object> instanceCache = new ConcurrentHashMap<>();

    /**
     * 系统SPI 目录
     * 
     */
    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system";

    /**
     * 用户自定义SPI 目录
     * 
     */
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom";

    /**
     * 扫描路径
     */
    private static final String[] SCAN_DIRS = new String[]{RPC_SYSTEM_SPI_DIR,RPC_CUSTOM_SPI_DIR};

    /**
     * 动态加载的类列表
     */
    private static final List<Class<?>> LOAD_CLASS_LIST = Arrays.asList(Serializer.class);

    /**
     * 加载所有类型
     * 
     */
    public static void loadAll() {
        log.info("开始加载所有SPI类型");
        for(Class<?> aClass  : LOAD_CLASS_LIST) {
            load(aClass);
        }
        log.info("加载所有SPI类型完成");
    }

    /**
     * 获取某个接口的实例
     * @param tClass 接口类
     * @param key 键
     * @param <T> 泛型
     * @return 实例
     */
    public static <T> T getInstance(Class<T> tClass,String key) {
        //根据接口名取出已加载的 key -> class 映射 loaderMap
        String tClassName = tClass.getName();
        Map<String,Class<?>> keyClassMap = loaderMap.get(tClassName);

        if(keyClassMap == null) {
            throw new RuntimeException(String.format("未找到%s的SPI实现类",tClassName));
        }
        if (!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("SpiLoader 的 %s 实现类中未找到 key: %s 的类型",tClassName,key));
        }
        //获取到要加载的实现类型
        Class<?> implClass = keyClassMap.get(key);
        //从实例缓存中加载指定的类型的实例
        String implClassName = implClass.getName();
        //若不存在则反射 new 实例
        if(!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | java.lang.reflect.InvocationTargetException | NoSuchMethodException e) {
                String msg = String.format("%s 的实例化失败",implClassName);
                throw new RuntimeException(msg,e);
            }
        }
        return tClass.cast(instanceCache.get(implClassName));
    }

    /**
     * 加载某个类型
     * @param loadClass 要加载的类型
     * @throws IOException 
     */
    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("开始加载{}的SPI实现类",loadClass.getName());
        //扫描路径，用户自定义的SPI优先级高于系统SPI
        Map<String,Class<?>> keyClassMap = new HashMap<>();
        for(String scanDir : SCAN_DIRS) {
            //构建扫描路径，获取到所有资源
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            //读取每个资源文件
            for(URL resource : resources) {
                try {
                    /** 打开文件输入流，支持自动关闭（try-with-resources），防止内存泄漏。
                     * InputStreamReader 将二进制流变成字符流；
                     * BufferedReader 用来逐行读取。
                     */
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while((line = bufferedReader.readLine()) != null) {
                        String[] strArray = line.split("=");
                        if(strArray.length > 1) {
                            String key = strArray[0];
                            String value = strArray[1];
                            //利用 Class.forName(value) 反射加载类对象，并注册进当前接口类的 key -> class 映射表中
                            keyClassMap.put(key, Class.forName(value));
                        }
                    }
                } catch (Exception e) {
                    log.error("加载{}的SPI实现类失败",loadClass.getName(),e);
                }
            }
        }            
        loaderMap.put(loadClass.getName(), keyClassMap);
        return keyClassMap;
    }

    public static void main(String[] args) throws IOException ,ClassNotFoundException{
        load(Serializer.class);
        System.out.println(loaderMap);
        System.out.println(getInstance(Serializer.class, "e"));
    }
}
