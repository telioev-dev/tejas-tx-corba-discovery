package com.teliolabs.corba.data.repository;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.domain.PTPEntity;
import com.teliolabs.corba.data.dto.Equipment;
import com.teliolabs.corba.data.dto.PTP;
import com.teliolabs.corba.data.exception.DataAccessException;
import com.teliolabs.corba.data.mapper.ResultSetMapperFunction;
import com.teliolabs.corba.data.queries.EquipmentQueries;
import com.teliolabs.corba.data.queries.PTPQueries;
import com.teliolabs.corba.utils.DBUtils;
import com.teliolabs.corba.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class PTPRepository extends GenericRepository<PTPEntity> {

    private static final PTPRepository INSTANCE = new PTPRepository();

    public static PTPRepository getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getTableName() {
        return DBUtils.getTable(DiscoveryItemType.PTP);
    }

    @Override
    protected void setPreparedStatementParameters(PreparedStatement preparedStatement, PTPEntity terminationPoint) throws SQLException {
        preparedStatement.setString(1, StringUtils.trimString(terminationPoint.getPtpId())); // ptp_id
        preparedStatement.setString(2, StringUtils.trimString(terminationPoint.getPortLocation())); // port_location
        preparedStatement.setString(3, StringUtils.trimString(terminationPoint.getMeName())); // me_name
        preparedStatement.setString(4, StringUtils.trimString(terminationPoint.getMeLabel())); // me_label
        preparedStatement.setString(5, StringUtils.trimString(terminationPoint.getProductName())); // product_name
        preparedStatement.setString(6, StringUtils.trimString(terminationPoint.getPortNativeName())); // port_native_name
        preparedStatement.setString(7, StringUtils.trimString(terminationPoint.getSlot())); // slot
        preparedStatement.setString(8, StringUtils.trimString(terminationPoint.getRate())); // rate
        preparedStatement.setString(9, StringUtils.trimString(terminationPoint.getType())); // type
        preparedStatement.setString(10, StringUtils.trimString(terminationPoint.getTraceTx())); // trace_tx
        preparedStatement.setString(11, StringUtils.trimString(terminationPoint.getTraceRx())); // trace_rx
        preparedStatement.setTimestamp(12, Timestamp.from(terminationPoint.getLastModifiedDate().toInstant())); // last_modified_date
    }

    public void deleteTerminationPoints(List<PTP> terminationPointsToDelete) {

        if (terminationPointsToDelete == null || terminationPointsToDelete.isEmpty()) {
            log.warn("No PTPs provided for deletion.");
            return;
        }

        String sql = String.format(PTPQueries.SOFT_DELETE_SQL, getTableName());
        log.info("PTP Soft delete SQL: {}", sql);

        int batchSize = 50;
        int totalDeleted = 0;
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false); // Disable auto-commit for batch processing
            int batchCounter = 0;
            for (int i = 0; i < terminationPointsToDelete.size(); i++) {
                PTP terminationPoint = terminationPointsToDelete.get(i);
                ps.setTimestamp(1, Timestamp.from(terminationPoint.getDeltaTimestamp().toInstant()));
                ps.setString(2, terminationPoint.getMeName());
                ps.setString(3, terminationPoint.getPtpId());
                ps.addBatch();
                batchCounter++;
                if (batchCounter == batchSize) {
                    totalDeleted += executeBatch(ps, connection);
                    batchCounter = 0;
                }
            }

            // Execute remaining batch
            if (batchCounter > 0) {
                totalDeleted += executeBatch(ps, connection);
            }
            log.info("Total PTPss deleted: {}", totalDeleted);
        } catch (SQLException e) {
            log.error("Error during PTP deletion, rolling back transaction.", e);
            throw new DataAccessException("Failed to delete EQs.", e);
        }
    }

    public int upsertTerminationPoints(List<PTP> terminationPoints, int batchSize) throws SQLException {
        if (terminationPoints == null || terminationPoints.isEmpty()) {
            return 0;
        }

        String upsertSQL = String.format(PTPQueries.UPSERT_SQL, getTableName(), getTableName());
        log.info("upsertSQL: {}", upsertSQL);
        int totalInserted = 0;

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(upsertSQL)) {
                int batchCounter = 0;

                for (PTP entity : terminationPoints) {
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
            } catch (SQLException e) {
                connection.rollback();
                log.error("Error inserting entities. Transaction rolled back.");
                throw e;
            }
        } catch (SQLException e) {
            log.error("Error establishing database connection.");
            throw e;
        }

        return totalInserted;
    }

    public <T> List<T> findAllTerminationPoints(ResultSetMapperFunction<ResultSet, T> mapperFunction) {

        String tableName = DBUtils.getTable(DiscoveryItemType.PTP);
        String sql = String.format(PTPQueries.SELECT_ALL_SQL, tableName);

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


    public int insertTerminationPoints(List<PTP> terminationPoints, int batchSize) throws SQLException {
        if (terminationPoints == null || terminationPoints.isEmpty()) {
            log.warn("No PTPs to insert.");
            return 0;
        }

        String tableName = DBUtils.getTable(DiscoveryItemType.PTP);
        String sql = String.format(PTPQueries.INSERT_PTP_SQL, tableName);

        int totalInserted = 0;

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int batchCounter = 0;
                for (PTP ptp : terminationPoints) {
                    setPreparedStatementParameters(ps, ptp);
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
                log.error("Error inserting PTPs. Transaction rolled back.", e);
                throw e;
            }
        } catch (SQLException e) {
            log.error("Error establishing database connection.", e);
            throw e;
        }

        return totalInserted;
    }

    private void setPreparedStatementParameters(PreparedStatement preparedStatement, PTP terminationPoint) throws SQLException {
        ZonedDateTime executionTimestamp = ExecutionContext.getInstance().getExecutionTimestamp();
        preparedStatement.setString(1, StringUtils.trimString(terminationPoint.getPtpId())); // ptp_id
        preparedStatement.setString(2, StringUtils.trimString(terminationPoint.getPortLocation())); // port_location
        preparedStatement.setString(3, StringUtils.trimString(terminationPoint.getMeName())); // me_name
        preparedStatement.setString(4, StringUtils.trimString(terminationPoint.getMeLabel())); // me_label
        preparedStatement.setString(5, StringUtils.trimString(terminationPoint.getProductName())); // product_name
        preparedStatement.setString(6, StringUtils.trimString(terminationPoint.getPortNativeName())); // port_native_name
        preparedStatement.setString(7, StringUtils.trimString(terminationPoint.getSlot())); // slot
        preparedStatement.setString(8, StringUtils.trimString(terminationPoint.getRate())); // rate
        preparedStatement.setString(9, StringUtils.trimString(terminationPoint.getType())); // type
        preparedStatement.setString(10, StringUtils.trimString(terminationPoint.getTraceTx())); // trace_tx
        preparedStatement.setString(11, StringUtils.trimString(terminationPoint.getTraceRx())); // trace_rx
        preparedStatement.setTimestamp(12, Timestamp.from(terminationPoint.getLastModifiedDate().toInstant())); // last_modified_date
        ExecutionMode executionMode = ExecutionContext.getInstance().getExecutionMode();
        if (executionMode == ExecutionMode.DELTA) {
            preparedStatement.setTimestamp(13, Timestamp.from(executionTimestamp.toInstant()));
        }
    }
}