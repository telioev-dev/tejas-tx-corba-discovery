package com.teliolabs.corba.data.repository;

import com.teliolabs.corba.config.DataSourceConfig;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.stream.Collectors;

@Log4j2
public class NiaRepository {

    private static NiaRepository instance;

    // Public method to get the singleton instance
    public static NiaRepository getInstance() {
        if (instance == null) {
            synchronized (NiaRepository.class) {
                if (instance == null) {
                    instance = new NiaRepository();
                }
            }
        }
        return instance;
    }

    public void executeViewFromFile(String viewSqlFileName, String viewName) throws SQLException {

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             Statement stmt = connection.createStatement();
             InputStream inputStream = NiaRepository.class.getClassLoader().getResourceAsStream(viewSqlFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String dropViewSql = "BEGIN "
                    + "  EXECUTE IMMEDIATE 'DROP MATERIALIZED VIEW " + viewName + "'; "
                    + "EXCEPTION "
                    + "  WHEN OTHERS THEN "
                    + "    IF SQLCODE != -12003 THEN RAISE; END IF; "
                    + "END;";
            log.info("Dropping materialized view if exists: {}", viewName);
            stmt.execute(dropViewSql);
            log.info("Materialized view: {} dropped successfully", viewName);
            // Read the entire SQL file into a single string
            String sql = reader.lines().collect(Collectors.joining("\n"));

            sql = sql.replace("{{VIEW_NAME}}", viewName);

            // Execute the SQL script
            stmt.execute(sql);

            log.info("Materialized view: {} created successfully!", viewName);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Exception executing view SQL: {}, error: {}", viewSqlFileName, e.getMessage());
        }
    }
}
