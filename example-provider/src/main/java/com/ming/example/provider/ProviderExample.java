package com.ming.example.provider;

import java.util.ArrayList;
import java.util.List;

import com.ming.example.common.service.UserService;
import com.ming.example.provider.service.impl.UserServiceImpl;
import com.ming.rpc.bootstrap.ProviderBootstrap;
import com.ming.rpc.model.ServiceRegisterInfo;


/**
 * 服务提供者示例
 */
public class ProviderExample {
    public static void main(String[] args) {
        // 要注册的服务
        List<ServiceRegisterInfo<?>> serviceRegisterInfoList = new ArrayList();
        ServiceRegisterInfo<UserService> serviceRegisterInfo = new ServiceRegisterInfo<>(UserService.class.getName(),UserServiceImpl.class);
        serviceRegisterInfoList.add(serviceRegisterInfo);
        // 服务提供者初始化
        ProviderBootstrap.init(serviceRegisterInfoList);
    }
}
