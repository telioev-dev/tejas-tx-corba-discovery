package com.teliolabs.corba.data.repository;


import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.dto.SNC;
import com.teliolabs.corba.data.dto.Topology;
import com.teliolabs.corba.data.exception.DataAccessException;
import com.teliolabs.corba.data.mapper.ResultSetMapperFunction;
import com.teliolabs.corba.data.queries.PTPQueries;
import com.teliolabs.corba.data.queries.SNCQueries;
import com.teliolabs.corba.data.queries.TopologyQueries;
import com.teliolabs.corba.utils.DBUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class SNCRepository {

    private static final SNCRepository INSTANCE = new SNCRepository();

    public static SNCRepository getInstance() {
        return INSTANCE;
    }

    public <T> List<T> findAllSNCs(ResultSetMapperFunction<ResultSet, T> mapperFunction) {

        String tableName = DBUtils.getTable(DiscoveryItemType.SNC);
        String sql = String.format(SNCQueries.SELECT_ALL_SQL, tableName);

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<T> results = new ArrayList<>();

            while (resultSet.next()) {
                results.add(mapperFunction.apply(resultSet));
            }

            return results;
        } catch (SQLException e) {
            log.error("Error fetching all managed elements", e);
            throw new DataAccessException("Error fetching all managed elements", e);
        }
    }

    public void truncate() {
        String tableName = DBUtils.getTable(DiscoveryItemType.SNC);
        String sql = String.format(SNCQueries.TRUNCATE_SQL, tableName);

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             Statement stmt = connection.createStatement()) {

            // Execute the TRUNCATE statement
            stmt.executeUpdate(sql);

            log.info("Table: {} truncated successfully", tableName);
        } catch (SQLException e) {
            log.error("Error truncating SNCs", e);
            throw new DataAccessException("Error truncating SNCs", e);
        }
    }

    public void upsertSNCs(List<SNC> sncList) throws SQLException {
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            String sequenceName = DBUtils.getSequence(DiscoveryItemType.SNC);
            String tableName = DBUtils.getTable(DiscoveryItemType.SNC);

            log.info("DiscoveryItemType.SNC SequenceName: {}", sequenceName);
            log.info("DiscoveryItemType.SNC TableName: {}", tableName);

            String sql = String.format(SNCQueries.UPSERT_SQL, tableName, tableName);
            connection.setAutoCommit(false); // Disable auto-commit for batch processing

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int count = 0;
                for (int i = 0; i < sncList.size(); i++) {
                    SNC snc = sncList.get(i);
                    setPreparedStatementParameters(ps, snc);
                    ps.addBatch();
                    // Execute batch after every 100 elements
                    boolean condition = (i + 1) % 100 == 0 || i == sncList.size() - 1;
                    if (condition) {
                        int[] batchResult = ps.executeBatch(); // Execute the batch
                        connection.commit();
                        count += batchResult.length;
                        ps.clearBatch(); // Clear the batch to start a new one
                    }
                }
                log.info("Total SNCs upserted: {}", count);
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback(); // Rollback in case of error
                throw e;
            }
        }
    }

    public int insertSNCs(List<SNC> sncs, int batchSize) throws SQLException {
        if (sncs == null || sncs.isEmpty()) {
            log.warn("No SNCs to insert.");
            return 0;
        }

        String tableName = DBUtils.getTable(DiscoveryItemType.SNC);
        String deleteSQL = String.format(SNCQueries.DELETE_ALL_SQL, tableName);
        String sql = String.format(SNCQueries.INSERT_SQL, tableName);


        int totalInserted = 0;
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {

                // Step 1: Execute DELETE statement
                //int rowsDeleted = deleteStatement.executeUpdate();
                //log.info("These many SNCs deleted successfully: {}", rowsDeleted);
                int batchCounter = 0;

                for (SNC snc : sncs) {
                    setPreparedStatementParameters(ps, snc);
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
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error inserting SNCs. Transaction rolled back.", e);
                throw e;
            }
        } catch (SQLException e) {
            log.error("Error establishing database connection.", e);
            throw e;
        }

        return totalInserted;
    }

    public void deleteSubnetworkConnections(List<String> sncsToBeDeleted) {

        if (sncsToBeDeleted == null || sncsToBeDeleted.isEmpty()) {
            log.warn("No SNCs provided for deletion.");
            return;
        }
        String tableName = DBUtils.getTable(DiscoveryItemType.SNC);
        String placeholders = String.join(",", Collections.nCopies(sncsToBeDeleted.size(), "?"));
        String sql = String.format(SNCQueries.SOFT_DELETE_SQL, tableName) + "(" + placeholders + ")";

        log.info("Soft delete SQL: {}", sql);

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false); // Disable auto-commit for batch processing

            for (int i = 0; i < sncsToBeDeleted.size(); i++) {
                String sncId = sncsToBeDeleted.get(i);
                log.info("sncId:delete: {}", sncId);
                ps.setString(i + 1, sncId);
            }

            int rowsUpdated = ps.executeUpdate();
            connection.commit();
            log.info("Total topologies deleted: {}", rowsUpdated);
        } catch (SQLException e) {
            log.error("Error during topology deletion, rolling back transaction.", e);
            throw new DataAccessException("Failed to delete topologies.", e);
        }
    }

    private void setPreparedStatementParameters(PreparedStatement ps, SNC snc) throws SQLException {
        ps.setString(1, snc.getSncId());
        ps.setString(2, snc.getSncName());
        ps.setObject(3, snc.getCircuitId(), Types.NUMERIC);
        ps.setObject(4, snc.getSrfId(), Types.NUMERIC);
        ps.setShort(5, snc.getSncRate());
        ps.setString(6, snc.getVCat());
        ps.setString(7, snc.getAEndMe());
        ps.setString(8, snc.getAEndMeLabel());
        ps.setString(9, snc.getAEndPtp());
        ps.setString(10, snc.getAEndPtpLabel());
        ps.setString(11, snc.getAEndChannel());
        ps.setString(12, snc.getZEndMe());
        ps.setString(13, snc.getZEndMeLabel());
        ps.setString(14, snc.getZEndPtp());
        ps.setString(15, snc.getZEndPtpLabel());
        ps.setString(16, snc.getZEndChannel());
        ps.setTimestamp(17, Timestamp.from(snc.getLastModifiedDate().toInstant()));
    }

    private int executeBatch(PreparedStatement ps, Connection connection) throws SQLException {
        int[] batchResults = ps.executeBatch();
        connection.commit();
        ps.clearBatch();
        return batchResults.length;
    }
}