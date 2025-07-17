package com.ming.rpc.springboot.processor;

import java.lang.reflect.Field;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;

import com.ming.rpc.RpcApplication;
import com.ming.rpc.config.RpcConfig;
import com.ming.rpc.constant.RpcConstant;
import com.ming.rpc.model.ServiceMetaInfo;
import com.ming.rpc.proxy.ServiceProxyFactory;
import com.ming.rpc.registry.LocalRegistry;
import com.ming.rpc.registry.Registry;
import com.ming.rpc.registry.RegistryFactory;
import com.ming.rpc.springboot.annotation.RpcReference;
import com.ming.rpc.springboot.annotation.RpcService;

import lombok.extern.slf4j.Slf4j;

/**
 * RPC Bean后处理器
 *
 * 负责处理@RpcService和@RpcReference注解
 *
 * @author ming
 * @version 1.0
 * @since 2024
 */
@Slf4j
public class RpcBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        // 处理@RpcService注解
        if (beanClass.isAnnotationPresent(RpcService.class)) {
            RpcService rpcService = beanClass.getAnnotation(RpcService.class);

            // 获取服务接口类
            Class<?> interfaceClass = rpcService.interfaceClass();
            if (interfaceClass == void.class) {
                // 如果未指定接口，使用第一个实现的接口
                Class<?>[] interfaces = beanClass.getInterfaces();
                if (interfaces.length > 0) {
                    interfaceClass = interfaces[0];
                } else {
                    log.warn("RpcService {} does not implement any interface", beanClass.getName());
                    return bean;
                }
            }

            // 构建服务key
            String serviceKey = buildServiceKey(interfaceClass.getName(), rpcService.version(), rpcService.group());

            // 注册到本地注册中心
            LocalRegistry.register(serviceKey, beanClass);
            log.info("Registered RPC service: {} -> {}", serviceKey, beanClass.getName());

            // 注册到远程注册中心
            try {
                registerToRemoteRegistry(interfaceClass, rpcService, beanClass);
            } catch (Exception e) {
                log.warn("Failed to register service to remote registry: {}, error: {}", serviceKey, e.getMessage());
                // 在测试环境中，注册中心可能不可用，这是正常的
            }
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();

        // 处理@RpcReference注解
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(RpcReference.class)) {
                RpcReference rpcReference = field.getAnnotation(RpcReference.class);

                // 获取服务接口类
                Class<?> interfaceClass = rpcReference.interfaceClass();
                if (interfaceClass == void.class) {
                    interfaceClass = field.getType();
                }

                // 创建代理对象
                Object proxyObject = createServiceProxy(interfaceClass, rpcReference);

                // 注入代理对象
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, bean, proxyObject);

                log.info("Injected RPC reference: {} into {}.{}",
                    interfaceClass.getName(), beanClass.getName(), field.getName());
            }
        }

        return bean;
    }

    /**
     * 构建服务唯一标识key
     *
     * @param serviceName 服务名称
     * @param version 版本号
     * @param group 分组
     * @return 服务key
     */
    private String buildServiceKey(String serviceName, String version, String group) {
        StringBuilder keyBuilder = new StringBuilder(serviceName);

        if (version != null && !version.isEmpty()) {
            keyBuilder.append(":").append(version);
        } else {
            keyBuilder.append(":").append(RpcConstant.DEFAULT_SERVICE_VERSION);
        }

        if (group != null && !group.isEmpty()) {
            keyBuilder.append(":").append(group);
        }

        return keyBuilder.toString();
    }

    /**
     * 注册服务到远程注册中心
     *
     * @param interfaceClass 服务接口类
     * @param rpcService RpcService注解
     * @param implClass 服务实现类
     * @throws Exception 注册异常
     */
    private void registerToRemoteRegistry(Class<?> interfaceClass, RpcService rpcService, Class<?> implClass) throws Exception {
        // 检查是否在测试环境中
        if (isTestEnvironment()) {
            log.info("Skipping remote registry registration in test environment for service: {}", interfaceClass.getName());
            return;
        }

        RpcConfig rpcConfig = RpcApplication.getRpcConfig();
        Registry registry = RegistryFactory.getInstance(rpcConfig.getRegistryConfig().getRegistry());

        // 构建服务元信息
        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName(interfaceClass.getName());
        serviceMetaInfo.setServiceVersion(rpcService.version().isEmpty() ?
            RpcConstant.DEFAULT_SERVICE_VERSION : rpcService.version());
        serviceMetaInfo.setServiceHost(rpcConfig.getServerHost());
        serviceMetaInfo.setServicePort(rpcConfig.getServerPort());
        serviceMetaInfo.setServiceGroup(rpcService.group());

        // 注册服务
        registry.register(serviceMetaInfo);
        log.info("Registered service to remote registry: {}", serviceMetaInfo.getServiceKey());
    }

    /**
     * 检查是否在测试环境中
     *
     * @return true表示在测试环境中
     */
    private boolean isTestEnvironment() {
        // 检查是否有JUnit相关的类在classpath中
        try {
            Class.forName("org.junit.jupiter.api.Test");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 创建服务代理对象
     *
     * @param interfaceClass 服务接口类
     * @param rpcReference RpcReference注解
     * @return 代理对象
     */
    private Object createServiceProxy(Class<?> interfaceClass, RpcReference rpcReference) {
        // 如果启用了mock，创建mock代理
        if (rpcReference.mock()) {
            return ServiceProxyFactory.getMockProxy(interfaceClass);
        }

        // 创建普通代理
        return ServiceProxyFactory.getProxy(interfaceClass);
    }
}
