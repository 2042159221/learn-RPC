package com.ming.rpc.loadbalancer;

import com.ming.rpc.model.ServiceMetaInfo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 一致性哈希负载均衡器测试
 */
public class ConsistenHashLoadBalancerTest {

    @Test
    public void select() {
        // 创建 ConsistenHashLoadBalancer 实例
        LoadBalancer loadBalancer = new ConsistenHashLoadBalancer();

        // 请求参数
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", "testMethod");

        // 服务列表
        ServiceMetaInfo serviceMetaInfo1 = new ServiceMetaInfo();
        serviceMetaInfo1.setServiceName("myService");
        serviceMetaInfo1.setServiceVersion("1.0");
        serviceMetaInfo1.setServiceHost("localhost");
        serviceMetaInfo1.setServicePort(8080);

        ServiceMetaInfo serviceMetaInfo2 = new ServiceMetaInfo();
        serviceMetaInfo2.setServiceName("myService");
        serviceMetaInfo2.setServiceVersion("1.0");
        serviceMetaInfo2.setServiceHost("example.com");
        serviceMetaInfo2.setServicePort(80);

        List<ServiceMetaInfo> serviceMetaInfoList = Arrays.asList(serviceMetaInfo1, serviceMetaInfo2);

        // 连续调用 5 次
        ServiceMetaInfo selectedService = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println("Selected: " + selectedService);
        Assert.assertNotNull(selectedService);

        selectedService = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println("Selected: " + selectedService);
        Assert.assertNotNull(selectedService);

        selectedService = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println("Selected: " + selectedService);
        Assert.assertNotNull(selectedService);
        
        selectedService = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println("Selected: " + selectedService);
        Assert.assertNotNull(selectedService);

        selectedService = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println("Selected: " + selectedService);
        Assert.assertNotNull(selectedService);
    }

    @Test
    public void selectWithSingleNode() {
        LoadBalancer loadBalancer = new ConsistenHashLoadBalancer();
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("methodName", "singleNodeTest");

        ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
        serviceMetaInfo.setServiceName("singleNodeService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(9090);

        List<ServiceMetaInfo> serviceMetaInfoList = Arrays.asList(serviceMetaInfo);
        ServiceMetaInfo selectedService = loadBalancer.select(requestParams, serviceMetaInfoList);
        System.out.println("Single Node Selected: " + selectedService);
        Assert.assertNotNull(selectedService);
        Assert.assertEquals(serviceMetaInfo, selectedService);
    }
} 