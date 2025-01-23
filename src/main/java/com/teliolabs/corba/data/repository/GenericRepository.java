package com.teliolabs.corba.data.repository;

import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.exception.DataAccessException;
import com.teliolabs.corba.data.mapper.ResultSetMapperFunction;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public abstract class GenericRepository<T> {

    protected abstract String getTableName();

    protected abstract void setPreparedStatementParameters(PreparedStatement ps, T entity) throws SQLException;

    public <T> List<T> findAll(ResultSetMapperFunction<ResultSet, T> mapperFunction, String selectSQLTemplate) {
        String tableName = getTableName();
        String sql = String.format(selectSQLTemplate, tableName);

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            List<T> results = new ArrayList<>();

            while (resultSet.next()) {
                results.add(mapperFunction.apply(resultSet));
            }

            return results;
        } catch (SQLException e) {
            log.error("Error fetching all records from table: {}", tableName, e);
            throw new DataAccessException("Error fetching all records", e);
        }
    }

    public int insertEntities(List<T> entities, String deleteSQLTemplate, String insertSQLTemplate, int batchSize) throws SQLException {
        if (entities == null || entities.isEmpty()) {
            System.out.println("No entities to insert.");
            return 0;
        }

        String deleteSQL = String.format(deleteSQLTemplate, getTableName());
        String insertSQL = String.format(insertSQLTemplate, getTableName());

        log.debug("deleteSQL: {}", deleteSQL);
        log.debug("insertSQL: {}", insertSQL);
        
        int totalInserted = 0;

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL); PreparedStatement ps = connection.prepareStatement(insertSQL)) {

                // Step 1: Execute DELETE statement
                int rowsDeleted = deleteStatement.executeUpdate();
                log.debug("Rows deleted successfully: {}", rowsDeleted);
                int batchCounter = 0;

                for (T entity : entities) {
                    setPreparedStatementParameters(ps, entity);
                    ps.addBatch();
                    batchCounter++;

                    if (batchCounter == batchSize) {
                        totalInserted += executeBatch(ps, connection);
                        batchCounter = 0;
                    }
                }

                // Execute remaining batch
                if (batchCounter > 0) {
                    totalInserted += executeBatch(ps, connection);
                }

                System.out.println("Total rows inserted: " + totalInserted);
            } catch (SQLException e) {
                connection.rollback();
                System.err.println("Error inserting entities. Transaction rolled back.");
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error establishing database connection.");
            throw e;
        }

        return totalInserted;
    }

    private int executeBatch(PreparedStatement ps, Connection connection) throws SQLException {
        int[] batchResults = ps.executeBatch();
        connection.commit();
        ps.clearBatch();
        return batchResults.length;
    }
}
