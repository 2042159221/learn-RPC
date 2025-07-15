package com.ming.rpc.registry;

import com.ming.rpc.model.ServiceMetaInfo;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostgreSqlRegistry extends AbstractDbRegistry {

    @Override
    protected String getCreateTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + DB_TABLE_NAME + " (" +
                "id SERIAL PRIMARY KEY," +
                "serviceName VARCHAR(255) NOT NULL," +
                "serviceVersion VARCHAR(255) NOT NULL," +
                "serviceHost VARCHAR(255) NOT NULL," +
                "servicePort INT NOT NULL," +
                "serviceGroup VARCHAR(255) NOT NULL," +
                "createTime TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP," +
                "updateTime TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP," +
                "CONSTRAINT unique_service UNIQUE (serviceName, serviceVersion, serviceHost, servicePort)" +
                ")";
    }

    @Override
    protected String getRegisterSql() {
        return "INSERT INTO " + DB_TABLE_NAME + " (serviceName, serviceVersion, serviceHost, servicePort, serviceGroup, updateTime) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (serviceName, serviceVersion, serviceHost, servicePort) DO UPDATE SET updateTime = CURRENT_TIMESTAMP";
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
        return "DELETE FROM " + DB_TABLE_NAME + " WHERE updateTime < CURRENT_TIMESTAMP - ? * INTERVAL '1 SECOND'";
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