package com.ming.rpc.spi;

import com.ming.rpc.serializer.Serializer;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import cn.hutool.core.io.resource.ResourceUtil;

/**
 * SPI加载器测试
 */
public class SpiLoaderTest {

    @Test
    public void testLoadSerializer() {
        // 加载序列化器SPI配置
        Map<String, Class<?>> keyClassMap = SpiLoader.load(Serializer.class);
        System.out.println("加载的SPI配置：" + keyClassMap);
    }

    @Test
    public void testResourceExists() {
        // 测试系统SPI目录
        String systemPath = "META-INF/rpc/system/com.ming.rpc.serializer.Serializer";
        List<URL> systemResources = ResourceUtil.getResources(systemPath);
        System.out.println("系统SPI资源：" + systemResources);
        
        // 测试自定义SPI目录
        String customPath = "META-INF/rpc/custom/com.ming.rpc.serializer.Serializer";
        List<URL> customResources = ResourceUtil.getResources(customPath);
        System.out.println("自定义SPI资源：" + customResources);
    }
    
    @Test
    public void testClassLoaderPaths() {
        // 打印类加载器的类路径
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        System.out.println("当前线程上下文类加载器：" + classLoader);
        
        // 尝试直接获取资源
        URL systemUrl = classLoader.getResource("META-INF/rpc/system/com.ming.rpc.serializer.Serializer");
        System.out.println("系统SPI资源URL：" + systemUrl);
        
        URL customUrl = classLoader.getResource("META-INF/rpc/custom/com.ming.rpc.serializer.Serializer");
        System.out.println("自定义SPI资源URL：" + customUrl);
    }
    
    @Test
    public void testReadSpiFileContent() throws IOException {
        // 读取系统SPI文件内容
        URL systemUrl = Thread.currentThread().getContextClassLoader()
                .getResource("META-INF/rpc/system/com.ming.rpc.serializer.Serializer");
        
        if (systemUrl != null) {
            System.out.println("读取系统SPI文件内容：");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(systemUrl.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        }
        
        // 读取自定义SPI文件内容
        URL customUrl = Thread.currentThread().getContextClassLoader()
                .getResource("META-INF/rpc/custom/com.ming.rpc.serializer.Serializer");
        
        if (customUrl != null) {
            System.out.println("读取自定义SPI文件内容：");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(customUrl.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }
        }
    }
    
    @Test
    public void testDebugSpiLoader() {
        // 测试SpiLoader.load方法的内部逻辑
        String className = Serializer.class.getName();
        System.out.println("加载的类名：" + className);
        
        for (String scanDir : new String[]{"META-INF/rpc/system/", "META-INF/rpc/custom/"}) {
            String path = scanDir + className;
            System.out.println("扫描路径：" + path);
            
            List<URL> resources = ResourceUtil.getResources(path);
            System.out.println("找到的资源：" + resources);
            
            for (URL resource : resources) {
                try {
                    System.out.println("处理资源：" + resource);
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        System.out.println("读取行：" + line);
                        String[] strArray = line.split("=");
                        if (strArray.length > 1) {
                            String key = strArray[0];
                            String value = strArray[1];
                            System.out.println("解析键值对：" + key + " = " + value);
                            try {
                                Class<?> clazz = Class.forName(value);
                                System.out.println("加载类成功：" + clazz);
                            } catch (ClassNotFoundException e) {
                                System.out.println("加载类失败：" + e.getMessage());
                            }
                        } else {
                            System.out.println("行格式不正确，无法解析键值对");
                        }
                    }
                } catch (IOException e) {
                    System.out.println("处理资源异常：" + e.getMessage());
                }
            }
        }
    }
} 