package com.ming.rpc.registry;

import com.ming.rpc.model.ServiceMetaInfo;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SqliteRegistry extends AbstractDbRegistry {

    @Override
    protected String getCreateTableSql() {
        return "CREATE TABLE IF NOT EXISTS " + DB_TABLE_NAME + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "serviceName TEXT NOT NULL," +
                "serviceVersion TEXT NOT NULL," +
                "serviceHost TEXT NOT NULL," +
                "servicePort INTEGER NOT NULL," +
                "serviceGroup TEXT NOT NULL," +
                "createTime TEXT DEFAULT (datetime('now','localtime'))," +
                "updateTime TEXT DEFAULT (datetime('now','localtime'))," +
                "UNIQUE(serviceName, serviceVersion, serviceHost, servicePort)" +
                ")";
    }

    @Override
    protected String getRegisterSql() {
        return "INSERT OR REPLACE INTO " + DB_TABLE_NAME + 
               " (serviceName, serviceVersion, serviceHost, servicePort, serviceGroup, updateTime) " +
               "VALUES (?, ?, ?, ?, ?, datetime('now','localtime'))";
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
        return "DELETE FROM " + DB_TABLE_NAME + " WHERE updateTime < datetime('now', '-' || ? || ' seconds')";
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