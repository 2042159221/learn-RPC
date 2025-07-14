## 第二阶段：SPI机制和工厂模式入门指南

### 什么是SPI机制？

SPI (Service Provider Interface) 是一种服务发现机制，允许程序在运行时动态地发现和加载实现了特定接口的类。在RPC框架中，我们可以使用SPI机制来实现不同组件（如序列化器、负载均衡器等）的可插拔扩展。

### 为什么需要SPI和工厂模式？

1. **灵活扩展**：用户可以自定义实现并且替换默认组件
2. **解耦**：框架核心与具体实现分离
3. **配置驱动**：通过配置文件选择不同实现



### 入手步骤

#### 第一步：了解基本概念

1. **SPI**：Java的SPI机制基于`META-INF/services/`目录下的配置文件
2. **工厂模式**：通过工厂类创建具体实现的实例

#### 第二步：创建SPI加载器

首先，我们需要创建一个通用的SPI加载器来处理扩展点的加载：

```java
package com.ming.rpc.spi;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SPI加载器，用于加载和缓存扩展点实现
 */
@Slf4j
public class SpiLoader {

    /**
     * 扩展点实例缓存
     * 第一层key是接口类，第二层key是具体实现的key，value是实现类的实例
     */
    private static final Map<String, Map<String, Object>> EXTENSION_INSTANCE_MAP = new ConcurrentHashMap<>();

    /**
     * 加载指定接口的指定实现
     *
     * @param clazz 接口类
     * @param key   实现类的key
     * @param <T>   接口类型
     * @return 实现类实例
     */
    public static <T> T load(Class<T> clazz, String key) {
        // 获取接口名称
        String className = clazz.getName();
        
        // 获取或初始化实现类缓存
        Map<String, Object> implementationMap = EXTENSION_INSTANCE_MAP.computeIfAbsent(className, k -> new ConcurrentHashMap<>());
        
        // 先从缓存中获取
        Object instance = implementationMap.get(key);
        if (instance != null) {
            return clazz.cast(instance);
        }
        
        // 使用Java原生SPI加载
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
        
        // 查找匹配的实现类
        for (T implementation : serviceLoader) {
            // 这里应该有个方法来匹配key和实现类，暂时简化处理，使用类名小写后的最后一部分作为key
            String implClassName = implementation.getClass().getSimpleName().toLowerCase();
            if (implClassName.contains(key.toLowerCase())) {
                // 缓存并返回实例
                implementationMap.put(key, implementation);
                return implementation;
            }
        }
        
        throw new IllegalArgumentException("No implementation found for " + className + " with key " + key);
    }
    
    /**
     * 获取指定接口的默认实现
     *
     * @param clazz 接口类
     * @param <T>   接口类型
     * @return 默认实现类实例
     */
    public static <T> T loadFirst(Class<T> clazz) {
        // 使用Java原生SPI加载
        ServiceLoader<T> serviceLoader = ServiceLoader.load(clazz);
        
        // 返回第一个实现
        for (T implementation : serviceLoader) {
            String key = implementation.getClass().getSimpleName().toLowerCase();
            
            // 缓存实例
            EXTENSION_INSTANCE_MAP
                .computeIfAbsent(clazz.getName(), k -> new ConcurrentHashMap<>())
                .put(key, implementation);
                
            return implementation;
        }
        
        throw new IllegalArgumentException("No implementation found for " + clazz.getName());
    }
}
```

#### 第三步：创建工厂类模板

以序列化器工厂为例，我们可以创建一个工厂类：

```java
package com.ming.rpc.serializer;

import com.ming.rpc.spi.SpiLoader;

/**
 * 序列化器工厂，用于创建序列化器实例
 */
public class SerializerFactory {
    
    /**
     * 获取序列化器实例
     *
     * @param key 序列化器类型
     * @return 序列化器实例
     */
    public static Serializer getInstance(String key) {
        return SpiLoader.load(Serializer.class, key);
    }
    
    /**
     * 获取默认序列化器实例
     *
     * @return 默认序列化器实例
     */
    public static Serializer getDefaultInstance() {
        return SpiLoader.loadFirst(Serializer.class);
    }
}
```

#### 第四步：创建接口定义

接下来，我们需要为各个扩展点创建接口：

以序列化器为例：

```java
package com.ming.rpc.serializer;

/**
 * 序列化器接口
 */
public interface Serializer {
    
    /**
     * 序列化
     *
     * @param obj 要序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serialize(Object obj);
    
    /**
     * 反序列化
     *
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @param <T>   目标类型
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}
```

#### 第五步：创建实现类

为每个接口创建一个或多个实现类：

```java
package com.ming.rpc.serializer.impl;

import com.ming.rpc.serializer.Serializer;

import java.io.*;

/**
 * JDK序列化器实现
 */
public class JdkSerializer implements Serializer {
    
    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("序列化失败", e);
        }
    }
    
    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bais)) {
            Object obj = ois.readObject();
            return clazz.cast(obj);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("反序列化失败", e);
        }
    }
}
```

#### 第六步：创建SPI配置文件

在`META-INF/services/`目录下创建以接口全限定名为文件名的配置文件：

文件路径：`META-INF/services/com.ming.rpc.serializer.Serializer`
内容：

```
com.ming.rpc.serializer.impl.JdkSerializer
```

### 如何逐步实现？

1. **从一个扩展点开始**：先实现序列化器，这是最简单的一个扩展点
   - 创建Serializer接口
   - 实现JdkSerializer类
   - 创建SerializerFactory工厂类
   - 创建SPI配置文件

2. **编写单元测试**：确保基本功能正常工作

3. **扩展到其他组件**：按照同样的模式实现其他扩展点
   - 负载均衡器
   - 注册中心
   - 重试策略
   - 容错策略

### 简化的实现步骤

1. **先创建SpiLoader类**：这是所有工厂的基础

2. **实现一个简单的扩展点**：先实现序列化器扩展点
   - Serializer接口
   - JdkSerializer实现类
   - SerializerFactory工厂类
   - SPI配置文件

3. **测试扩展点加载**：确保SPI机制正常工作

4. **逐步添加其他扩展点**：一个一个地实现其他扩展点

### 建议的开发顺序

1. SPI加载器 (SpiLoader)
2. 序列化器 (Serializer)
3. 负载均衡器 (LoadBalancer)
4. 注册中心 (Registry)
5. 重试策略 (RetryStrategy)
6. 容错策略 (TolerantStrategy)