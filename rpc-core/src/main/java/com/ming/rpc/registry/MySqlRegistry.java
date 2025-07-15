package com.ming.rpc.registry;

import com.ming.rpc.model.ServiceMetaInfo;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MySqlRegistry extends AbstractDbRegistry {

    @Override
    protected String getCreateTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + DB_TABLE_NAME + " (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "serviceName VARCHAR(255) NOT NULL," +
                "serviceVersion VARCHAR(255) NOT NULL," +
                "serviceHost VARCHAR(255) NOT NULL," +
                "servicePort INT NOT NULL," +
                "serviceGroup VARCHAR(255) NOT NULL," +
                "createTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updateTime TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "UNIQUE KEY unique_service (serviceName, serviceVersion, serviceHost, servicePort)" +
                ")";
    }

    @Override
    protected String getRegisterSql() {
        return "INSERT INTO " + DB_TABLE_NAME + " (serviceName, serviceVersion, serviceHost, servicePort, serviceGroup) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE updateTime = CURRENT_TIMESTAMP";
    }

    @Override
    protected String getUnregisterSql() {
        return "DELETE FROM " + DB_TABLE_NAME + " WHERE serviceName = ? AND serviceVersion = ? AND serviceHost = ? AND servicePort = ?";
    }

    @Override
    protected String getDiscoverySql() {
        return "SELECT serviceName, serviceVersion, serviceHost, servicePort, serviceGroup FROM " + DB_TABLE_NAME + " WHERE serviceName = ?";
    }

    @Override
    protected String getCleanupSql() {
        return "DELETE FROM " + DB_TABLE_NAME + " WHERE updateTime < NOW() - INTERVAL ? SECOND";
    }

    @Override
    protected void populateRegisterStatement(PreparedStatement ps, ServiceMetaInfo serviceMetaInfo) throws SQLException {
        ps.setString(1, serviceMetaInfo.getServiceName());
        ps.setString(2, serviceMetaInfo.getServiceVersion());
        ps.setString(3, serviceMetaInfo.getServiceHost());
        ps.setInt(4, serviceMetaInfo.getServicePort());
        ps.setString(5, serviceMetaInfo.getServiceGroup());
    }
} 