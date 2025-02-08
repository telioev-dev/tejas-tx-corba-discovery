package com.teliolabs.corba.application.metadata;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.repository.NiaRepository;
import com.teliolabs.corba.utils.DBUtils;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
public class TableManager {

    private static final TableManager INSTANCE = new TableManager();

    public static TableManager getInstance() {
        return INSTANCE;
    }

    private final String sqlDirectory = "sqls"; // Base directory

    /**
     * Ensures the table exists. If it doesn't, creates it.
     *
     * @param tableName the name of the table
     * @param createSQL the SQL statement to create the table
     * @throws SQLException if a database access error occurs
     */
    public void ensureTableExists(String tableName, String createSQL) throws SQLException {
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            if (!doesTableExist(connection, tableName)) {
                createTable(connection, createSQL);
            }
        }
    }

    /**
     * Checks if the table exists in the database.
     *
     * @param connection the database connection
     * @param tableName  the name of the table to check
     * @return true if the table exists, false otherwise
     * @throws SQLException if a database access error occurs
     */
    private boolean doesTableExist(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(null, null, tableName.toUpperCase(), null)) {
            return resultSet.next();
        }
    }

    private boolean doesTableExistForEntity(String vendor, String circle, String entity) {
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            return doesTableExist(connection, DBUtils.getTable(vendor, circle, entity));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the table using the provided SQL statement.
     *
     * @param connection the database connection
     * @param createSQL  the SQL statement to create the table
     * @throws SQLException if a database access error occurs
     */
    private void createTable(Connection connection, String createSQL) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(createSQL)) {
            statement.execute();
            System.out.println("Table created successfully: " + createSQL);
        }
    }

    /**
     * @param circle
     */
    public void createTablesForCircle(String circle) {
        String circlePath = sqlDirectory + "/" + circle;

        try {
            List<Path> sqlFiles = Files.walk(Paths.get(circlePath))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .sorted()
                    .collect(Collectors.toList());

            log.info("Found {} SQL files for circle: {}", sqlFiles.size(), circle);

            try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
                 Statement stmt = connection.createStatement()) {
                for (Path sqlFile : sqlFiles) {
                    executeSqlFile(sqlFile, stmt);
                }
                log.info("Table creation completed for circle: {}", circle);
            }
        } catch (IOException e) {
            log.error("Error reading SQL files for circle: {}", circle, e);
        } catch (SQLException e) {
            log.error("Error executing SQL for circle: {}", circle, e);
        }
    }

    // Creates a table for a specific entity within a circle
    public void createTableForEntity(String vendor, String circle, String entity) throws URISyntaxException {
        if (doesTableExistForEntity(vendor, circle, entity)) {
            log.info("Table exists for entity");
            return;
        }
        String sqlFilePath = sqlDirectory + "/" + circle + "/" + entity + ".sql";
        log.info("sqlFilePath: {}", sqlFilePath);

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             Statement stmt = connection.createStatement();
             InputStream inputStream = TableManager.class.getClassLoader().getResourceAsStream(sqlFilePath)) {
            assert inputStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

                String sql = reader.lines().collect(Collectors.joining("\n"));
                stmt.execute(sql);
                log.info("Table created successfully for entity: {} in circle: {}", entity, circle);
            }
        } catch (SQLException e) {
            log.error("Error executing SQL for entity: {} in circle: {}", entity, circle, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to execute SQL from a file
    private void executeSqlFile(Path sqlFile, Statement stmt) throws SQLException {
        try {
            String sql = new String(Files.readAllBytes(sqlFile));
            log.info("Executing SQL file: {}", sqlFile.getFileName());
            stmt.execute(sql);
        } catch (IOException e) {
            log.error("Error reading SQL file: {}", sqlFile, e);
        }
    }
}

