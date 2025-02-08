package com.teliolabs.corba.data.repository;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.domain.ManagedElementEntity;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.dto.Topology;
import com.teliolabs.corba.data.exception.DataAccessException;
import com.teliolabs.corba.data.mapper.ManagedElementResultSetMapper;
import com.teliolabs.corba.data.mapper.ResultSetMapperFunction;
import com.teliolabs.corba.data.mapper.TopologyResultSetMapper;
import com.teliolabs.corba.data.queries.EquipmentQueries;
import com.teliolabs.corba.data.queries.ManagedElementQueries;
import com.teliolabs.corba.utils.DBUtils;
import com.teliolabs.corba.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class ManagedElementRepository extends GenericRepository<ManagedElementEntity> {

    // Singleton instance
    private static final ManagedElementRepository INSTANCE = new ManagedElementRepository();


    // Public method to get the instance
    public static ManagedElementRepository getInstance() {
        return INSTANCE;
    }


    @Override
    protected String getTableName() {
        return DBUtils.getTable(DiscoveryItemType.ME);
    }


    @Override
    protected void setPreparedStatementParameters(PreparedStatement ps, ManagedElementEntity entity) throws SQLException {

    }


    public List<ManagedElementEntity> findAllManagedElements(boolean fetchDeleted) {
        return findAll(ManagedElementResultSetMapper.getInstance()::mapToEntity, fetchDeleted ? ManagedElementQueries.SELECT_ALL_SQL : ManagedElementQueries.SELECT_ALL_NON_DELETED_SQL);
    }


    public <T> List<T> findAllManagedElements(ResultSetMapperFunction<ResultSet, T> mapperFunction, boolean excludeDeleted) {
        log.info("findAllManagedElements - excludeDeleted: {}", excludeDeleted);
        return findAll(mapperFunction, excludeDeleted ? ManagedElementQueries.SELECT_ALL_NON_DELETED_SQL : ManagedElementQueries.SELECT_ALL_SQL);
    }


    private ManagedElementEntity mapResultSetToManagedElementEntity(ResultSet resultSet) throws SQLException {
        return ManagedElementResultSetMapper.getInstance().mapToEntity(resultSet);
    }

    private ManagedElement mapResultSetToManagedElementDto(ResultSet resultSet) throws SQLException {
        return ManagedElementResultSetMapper.getInstance().mapToDto(resultSet);
    }

    public void deleteManagedElements(List<String> managedElementsToDelete, boolean performHardDelete) {

        String tableName = DBUtils.getTable(DiscoveryItemType.ME);
        String sql = String.format(performHardDelete ? ManagedElementQueries.HARD_DELETE_SQL : ManagedElementQueries.SOFT_DELETE_SQL, tableName) + "(" +
                String.join(",", Collections.nCopies(managedElementsToDelete.size(), "?")) + ")";

        if (performHardDelete) {
            log.info("Hard delete SQL: {}", sql);
        } else {
            log.info("Soft delete SQL: {}", sql);
        }

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit for batch processing
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                // Set the me_names to be deleted in the batch
                for (int i = 0; i < managedElementsToDelete.size(); i++) {
                    ps.setString(i + 1, managedElementsToDelete.get(i));
                }
                // Execute the batch update to mark records as deleted
                int rowsUpdated = ps.executeUpdate();
                log.info("Total MEs deleted: {}", rowsUpdated);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int[] insertManagedElements(List<ManagedElement> managedElements) throws SQLException {
        String sequenceName = DBUtils.getSequence(DiscoveryItemType.ME);
        String tableName = DBUtils.getTable(DiscoveryItemType.ME);
        String deleteSQL = String.format(ManagedElementQueries.DELETE_ALL_SQL, tableName);

        log.debug("DiscoveryItemType.ME SequenceName: {}", sequenceName);

        String sql = String.format(ManagedElementQueries.INSERT_SQL, tableName);

        log.debug("sql: {}", sql);

        int[] result = new int[managedElements.size()];

        log.debug("Total records to be inserted: {}", managedElements.size());
        // Get a connection from the pool
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit for batch processing
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);
                 PreparedStatement stmt = connection.prepareStatement(sql)) {
                // Step 1: Execute DELETE statement
                int rowsDeleted = deleteStatement.executeUpdate();
                log.info("These many MEs deleted successfully: {}", rowsDeleted);

                int count = 0;
                for (int i = 0; i < managedElements.size(); i++) {

                    ManagedElement managedElement = managedElements.get(i);

                    stmt.setString(1, StringUtils.trimString(managedElement.getNativeEmsName()));
                    stmt.setString(2, StringUtils.trimString(managedElement.getMeName()));
                    stmt.setString(3, StringUtils.trimString(managedElement.getUserLabel()));
                    stmt.setString(4, StringUtils.trimString(managedElement.getProductName()));
                    stmt.setString(5, StringUtils.trimString(managedElement.getIpAddress()));
                    stmt.setString(6, StringUtils.trimString(managedElement.getSoftwareVersion()));
                    stmt.setString(7, StringUtils.trimString(managedElement.getLocation()));
                    stmt.setInt(8, managedElement.getCommunicationState().getState());
                    stmt.setString(9, StringUtils.trimString(managedElement.getCircle()));
                    stmt.setTimestamp(10, Timestamp.from(managedElement.getLastModifiedDate().toInstant()));

                    stmt.addBatch(); // Add to batch

                    // Execute batch after every 100 elements
                    boolean condition = (i + 1) % 100 == 0 || i == managedElements.size() - 1;
                    //log.info("Condition: {}", condition);
                    if (condition) {
                        int[] batchResult = stmt.executeBatch(); // Execute the batch
                        connection.commit();
                        System.arraycopy(batchResult, 0, result, count, batchResult.length); // Store the result
                        count += batchResult.length;
                        stmt.clearBatch(); // Clear the batch to start a new one
                    }
                }
                log.debug("Total records inserted: {}", count);
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback(); // Rollback in case of error
                throw e;
            }
        }
        return result;
    }


    public void upsertManagedElements(List<ManagedElement> managedElements) throws SQLException {
        int[] result = new int[managedElements.size()];
        ExecutionContext executionContext = ExecutionContext.getInstance();

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            String sequenceName = DBUtils.getSequence(DiscoveryItemType.ME);
            String tableName = DBUtils.getTable(DiscoveryItemType.ME);

            log.info("DiscoveryItemType.ME TableName: {}", tableName);

            String sql = String.format(ManagedElementQueries.UPSERT_SQL, tableName, tableName);
            connection.setAutoCommit(false); // Disable auto-commit for batch processing

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                int count = 0;
                for (int i = 0; i < managedElements.size(); i++) {

                    ManagedElement managedElement = managedElements.get(i);

                    stmt.setString(1, StringUtils.trimString(managedElement.getNativeEmsName()));
                    stmt.setString(2, StringUtils.trimString(managedElement.getMeName()));
                    stmt.setString(3, StringUtils.trimString(managedElement.getUserLabel()));
                    stmt.setString(4, StringUtils.trimString(managedElement.getProductName()));
                    stmt.setString(5, StringUtils.trimString(managedElement.getIpAddress()));
                    stmt.setString(6, StringUtils.trimString(managedElement.getSoftwareVersion()));
                    stmt.setString(7, StringUtils.trimString(managedElement.getLocation()));
                    stmt.setInt(8, managedElement.getCommunicationState().getState());
                    stmt.setString(9, StringUtils.trimString(managedElement.getCircle()));
                    stmt.setTimestamp(10, Timestamp.from(managedElement.getLastModifiedDate().toInstant()));
                    stmt.setTimestamp(11, Timestamp.from(executionContext.getExecutionTimestamp().toInstant()));
                    stmt.addBatch();
                    // Execute batch after every 100 elements
                    boolean condition = (i + 1) % 100 == 0 || i == managedElements.size() - 1;
                    if (condition) {
                        int[] batchResult = stmt.executeBatch(); // Execute the batch
                        connection.commit();
                        System.arraycopy(batchResult, 0, result, count, batchResult.length); // Store the result
                        count += batchResult.length;
                        stmt.clearBatch(); // Clear the batch to start a new one
                    }
                }
                log.info("Total records upserted: {}", count);
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback(); // Rollback in case of error
                throw e;
            }
        }
    }
}