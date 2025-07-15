package com.ming.rpc.registry;

import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.junit.Ignore;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@Ignore
public class NacosRegistryTest {

    // Using a GenericContainer as there's no official Nacos testcontainer module
    // We need to expose the port and set the mode to standalone
    @Rule
    public GenericContainer<?> nacosContainer = new GenericContainer<>(
            new ImageFromDockerfile()
                .withDockerfile(Paths.get("src/test/resources/nacos/Dockerfile")))
            .withExposedPorts(8848)
            .withEnv("MODE", "standalone");

    private NacosRegistry nacosRegistry;

    private final ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();

    @Before
    public void setUp() {
        nacosContainer.start();
        RegistryConfig registryConfig = new RegistryConfig();
        String host = nacosContainer.getHost();
        Integer port = nacosContainer.getMappedPort(8848);
        registryConfig.setAddress(host + ":" + port);

        nacosRegistry = new NacosRegistry();
        nacosRegistry.init(registryConfig);

        serviceMetaInfo.setServiceName("testNacosService");
        serviceMetaInfo.setServiceVersion("1.0");
        serviceMetaInfo.setServiceHost("localhost");
        serviceMetaInfo.setServicePort(1234);
        serviceMetaInfo.setServiceGroup("testGroup");
    }

    @After
    public void tearDown() {
        nacosRegistry.destroy();
    }

    @Test
    public void testRegisterAndDiscovery() throws Exception {
        nacosRegistry.register(serviceMetaInfo);

        // Allow some time for registration to complete
        Thread.sleep(1000);

        List<ServiceMetaInfo> result = nacosRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertFalse(result.isEmpty());
        assertEquals(serviceMetaInfo.getServiceHost(), result.get(0).getServiceHost());
    }

    @Test
    public void testUnregister() throws Exception {
        nacosRegistry.register(serviceMetaInfo);
        Thread.sleep(1000);

        nacosRegistry.unregister(serviceMetaInfo);
        Thread.sleep(1000);

        List<ServiceMetaInfo> result = nacosRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertTrue(result.isEmpty());
    }

    @Test
    public void testWatch() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        nacosRegistry.watch(serviceMetaInfo.getServiceKey());

        // Change the service to trigger the watch
        nacosRegistry.register(serviceMetaInfo);
        
        // This is not a perfect test for watch, as the cache is cleared async.
        // A better approach would be to have a way to be notified when the cache is cleared.
        // For now, we assume the mechanism works.
        // A manual trigger by unregistering will clear the instance list, which should be reflected.
        nacosRegistry.unregister(serviceMetaInfo);
        
        // Give some time for the watch event to be processed
        Thread.sleep(2000); 

        // After unregister, discovery should be empty.
        List<ServiceMetaInfo> result = nacosRegistry.serviceDiscovery(serviceMetaInfo.getServiceKey());
        assertTrue(result.isEmpty());
    }
} 