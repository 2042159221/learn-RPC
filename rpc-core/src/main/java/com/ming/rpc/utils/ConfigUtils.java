package com.ming.rpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;

/**
 * 配置工具类
 * description: 主要实现了从属性文件加载配置到Java对象的功能
 * 使用了Hutool库中的Props类来读取properties文件，并使用toBean方法将配置映射到Java对象中
 * 
 * @author ming
 * @date 2025-07-10
 * @version 1.0
 * @since 1.0
 * @see 
 */
public class ConfigUtils {
    /**
     * 加载配置对象
     * @param tClass 配置类
     * @param prefix 配置前缀
     * @param <T> 配置类
     * @return 配置对象
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix) {
        
        return loadConfig(tClass, prefix, "");
    }

    /**
     * 加载配置对象，支持区分环境
     * @param tClass 配置类
     * @param prefix 配置前缀
     * @param environment 环境
     * @param <T> 配置类
     * @return 配置对象
     */
    public static <T> T loadConfig(Class<T> tClass, String prefix, String environment) {
        StringBuilder configFileBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            configFileBuilder.append("-").append(environment);
        }
        configFileBuilder.append(".properties");
        Props props = new Props(configFileBuilder.toString());
        return props.toBean(tClass, prefix);
    }

}
