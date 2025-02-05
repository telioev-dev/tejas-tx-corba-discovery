package com.teliolabs.corba.config;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DbProfile;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Log4j2
public class DataSourceConfig {
    private static final String CONFIG_FILE = "db_profiles.properties";
    private static HikariDataSource hikariDataSource;

    // Private constructor to prevent instantiation
    private DataSourceConfig() {
    }

    public static DataSource getHikariDataSource() {
        if (hikariDataSource == null) {
            synchronized (DataSourceConfig.class) {
                if (hikariDataSource == null) {
                    DbProfile dbProfile = ExecutionContext.getInstance().getDbProfile();
                    HikariConfig hikariConfig = getHikariConfig(dbProfile.name());

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

    private static HikariConfig getHikariConfig(String profile) {
        Properties properties = new Properties();

        try (InputStream input = DataSourceConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Database profile configuration file not found: " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading database profile configuration", e);
        }

        log.debug("DB Profile: {} loaded successfully.", profile);

        String url = properties.getProperty(profile + ".url");
        String username = properties.getProperty(profile + ".username");
        String password = properties.getProperty(profile + ".password");

        if (url == null || username == null || password == null) {
            throw new RuntimeException("Database profile not found or incomplete: " + profile);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        // Optional configuration settings
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(300000);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setMaxLifetime(1800000);

        return hikariConfig;
    }
}

