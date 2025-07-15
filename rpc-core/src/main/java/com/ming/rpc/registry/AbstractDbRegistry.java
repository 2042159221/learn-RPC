package com.ming.rpc.registry;

import com.ming.rpc.config.RegistryConfig;
import com.ming.rpc.model.ServiceMetaInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractDbRegistry implements Registry {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDbRegistry.class);

    protected Connection connection;

    /**
     * 表名
     */
    protected static final String DB_TABLE_NAME = "rpc_services";

    /**
     * 心跳续约时间（秒）
     */
    private static final int HEARTBEAT_TTL = 30;

    private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();


    @Override
    public void init(RegistryConfig registryConfig) {
        try {
            this.connection = DriverManager.getConnection(registryConfig.getAddress(), registryConfig.getUsername(), registryConfig.getPassword());
            createTableIfNotExists();
            // 启动心跳检测清理线程
            startHeartbeatCleanupTask();
        } catch (SQLException e) {
            LOGGER.error("Failed to connect to database for registry", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        String sql = getRegisterSql();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            populateRegisterStatement(ps, serviceMetaInfo);
            ps.executeUpdate();
        }
    }

    @Override
    public void unregister(ServiceMetaInfo serviceMetaInfo) {
        String sql = getUnregisterSql();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, serviceMetaInfo.getServiceName());
            ps.setString(2, serviceMetaInfo.getServiceVersion());
            ps.setString(3, serviceMetaInfo.getServiceHost());
            ps.setInt(4, serviceMetaInfo.getServicePort());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("Failed to unregister service", e);
        }
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {
        String sql = getDiscoverySql();
        List<ServiceMetaInfo> serviceMetaInfoList = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            String serviceName = serviceKey.split(":")[0];
            ps.setString(1, serviceName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ServiceMetaInfo serviceMetaInfo = new ServiceMetaInfo();
                serviceMetaInfo.setServiceName(rs.getString("serviceName"));
                serviceMetaInfo.setServiceVersion(rs.getString("serviceVersion"));
                serviceMetaInfo.setServiceHost(rs.getString("serviceHost"));
                serviceMetaInfo.setServicePort(rs.getInt("servicePort"));
                serviceMetaInfo.setServiceGroup(rs.getString("serviceGroup"));
                serviceMetaInfoList.add(serviceMetaInfo);
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to discover service", e);
        }
        return serviceMetaInfoList;
    }

    @Override
    public void destroy() {
        if (heartbeatExecutor != null && !heartbeatExecutor.isShutdown()) {
            heartbeatExecutor.shutdown();
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.error("Failed to close database connection", e);
            }
        }
    }

    @Override
    public void heartbeat() {
        // 心跳由注册时自动更新 updateTime 实现，这里可以定期清理过期节点
        // 清理任务已在 init 中启动
    }

    @Override
    public void watch(String serviceKey) {
        LOGGER.warn("Database-based registry does not support watch functionality.");
        // no-op
    }

    private void createTableIfNotExists() {
        String sql = getCreateTableSql();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.error("Failed to create table for registry", e);
            throw new RuntimeException(e);
        }
    }

    private void startHeartbeatCleanupTask() {
        heartbeatExecutor.scheduleWithFixedDelay(() -> {
            try {
                String sql = getCleanupSql();
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setInt(1, HEARTBEAT_TTL);
                    int deletedRows = ps.executeUpdate();
                    if (deletedRows > 0) {
                        LOGGER.info("Cleaned up {} expired services", deletedRows);
                    }
                }
            } catch (SQLException e) {
                LOGGER.error("Heartbeat cleanup task failed", e);
            }
        }, HEARTBEAT_TTL, HEARTBEAT_TTL, TimeUnit.SECONDS);
    }

    // --- Abstract methods for SQL dialects ---

    protected abstract String getCreateTableSql();

    protected abstract String getRegisterSql();

    protected abstract String getUnregisterSql();

    protected abstract String getDiscoverySql();

    protected abstract String getCleanupSql();

    protected abstract void populateRegisterStatement(PreparedStatement ps, ServiceMetaInfo serviceMetaInfo) throws SQLException;
} 