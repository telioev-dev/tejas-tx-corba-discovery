package com.teliolabs.corba.config;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;


public class DataSourceConfig {

    private static HikariDataSource hikariDataSource;

    // Private constructor to prevent instantiation
    private DataSourceConfig() {
    }

    public static DataSource getHikariDataSource() {
        if (hikariDataSource == null) {
            synchronized (DataSourceConfig.class) {
                if (hikariDataSource == null) {
                    HikariConfig hikariConfig = getHikariConfig();

                    // Additional configurations if needed
                    hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
                    hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
                    hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

                    hikariDataSource = new HikariDataSource(hikariConfig);
                }
            }
        }
        return hikariDataSource;
    }

    private static HikariConfig getHikariConfig() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:oracle:thin:@//devnidb:1521/ttldevnidbpdb.db");
        hikariConfig.setUsername("eci_stitching");
        hikariConfig.setPassword("eci2025");

        // Optional configuration settings
        hikariConfig.setMaximumPoolSize(10);       // Max number of connections
        hikariConfig.setMinimumIdle(5);            // Min number of idle connections
        hikariConfig.setIdleTimeout(300000);       // Time to wait before releasing idle connections
        hikariConfig.setConnectionTimeout(30000);  // Time to wait for a connection from the pool
        hikariConfig.setMaxLifetime(1800000);      // Max lifetime of a connection
        return hikariConfig;
    }
}

