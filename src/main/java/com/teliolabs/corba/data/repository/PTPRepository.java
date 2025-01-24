package com.teliolabs.corba.data.repository;


import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.dto.PTP;
import com.teliolabs.corba.data.dto.Topology;
import com.teliolabs.corba.data.exception.DataAccessException;
import com.teliolabs.corba.data.mapper.ResultSetMapperFunction;
import com.teliolabs.corba.data.queries.ManagedElementQueries;
import com.teliolabs.corba.data.queries.PTPQueries;
import com.teliolabs.corba.utils.DBUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class PTPRepository {

    private static final PTPRepository INSTANCE = new PTPRepository();

    public static PTPRepository getInstance() {
        return INSTANCE;
    }

    public void truncate() {
        String tableName = DBUtils.getTable(DiscoveryItemType.PTP);
        String sql = String.format(PTPQueries.TRUNCATE_SQL, tableName);

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             Statement stmt = connection.createStatement()) {

            // Execute the TRUNCATE statement
            stmt.executeUpdate(sql);

            log.info("Table: {} truncated successfully", tableName);
        } catch (SQLException e) {
            log.error("Error truncating PTPs", e);
            throw new DataAccessException("Error truncating PTPs", e);
        }
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

                log.info("Total PTPs inserted: {}", totalInserted);
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
        preparedStatement.setString(1, terminationPoint.getPtpId()); // ptp_id
        preparedStatement.setString(2, terminationPoint.getPortLocation()); // port_location
        preparedStatement.setString(3, terminationPoint.getMeName()); // me_name
        preparedStatement.setString(4, terminationPoint.getMeLabel()); // me_label
        preparedStatement.setString(5, terminationPoint.getProductName()); // product_name
        preparedStatement.setString(6, terminationPoint.getPortNativeName()); // port_native_name
        preparedStatement.setString(7, terminationPoint.getSlot()); // slot
        preparedStatement.setString(8, terminationPoint.getRate()); // rate
        preparedStatement.setString(9, terminationPoint.getType()); // type
        preparedStatement.setString(10, terminationPoint.getTraceTx()); // trace_tx
        preparedStatement.setString(11, terminationPoint.getTraceTx()); // trace_rx
        preparedStatement.setTimestamp(12, Timestamp.from(terminationPoint.getLastModifiedDate().toInstant())); // last_modified_date
    }

    private int executeBatch(PreparedStatement ps, Connection connection) throws SQLException {
        int[] batchResults = ps.executeBatch();
        connection.commit();
        ps.clearBatch();
        return batchResults.length;
    }


    public int insertTopologiesOld(List<Topology> topologies, int batchSize) throws SQLException {
        if (topologies == null || topologies.isEmpty()) {
            log.warn("No topologies to insert.");
            return 0;
        }

        String sequenceName = DBUtils.getSequence(DiscoveryItemType.TOPOLOGY);
        String tableName = DBUtils.getTable(DiscoveryItemType.TOPOLOGY);
        log.info("DiscoveryItemType.TOPOLOGY SequenceName: {}", sequenceName);
        String sql = String.format(ManagedElementQueries.INSERT_SQL, tableName, sequenceName);
        log.info("Insert SQL: {}", sql);

        int totalInserted = 0;

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit for batch processing
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int batchCounter = 0;
                for (int i = 0; i < topologies.size(); i++) {
                    Topology topology = topologies.get(i);
                    ps.setLong(1, topology.getPk());
                    ps.setString(2, topology.getTpLinkName());
                    ps.setString(3, topology.getNativeEmsName());
                    ps.setInt(4, topology.getRate());
                    ps.setString(5, topology.getLinkType());
                    ps.setString(6, topology.getDirection());
                    ps.setString(7, topology.getAEndEms());
                    ps.setString(8, topology.getAEndMeName());
                    ps.setString(9, topology.getAEndMeLabel());
                    ps.setString(10, topology.getAEndPortName());
                    ps.setString(11, topology.getAEndPortLabel());
                    ps.setString(12, topology.getZEndEms());
                    ps.setString(13, topology.getZEndMeName());
                    ps.setString(14, topology.getZEndMeLabel());
                    ps.setString(15, topology.getZEndPortName());
                    ps.setString(16, topology.getZEndPortLabel());
                    ps.setString(17, topology.getUserLabel());
                    ps.setString(18, topology.getProtection());
                    ps.setString(19, topology.getRingName());
                    ps.setString(20, topology.getInconsistent());
                    ps.setString(21, topology.getTechnologyLayer());
                    ps.setString(22, topology.getCircle());
                    ps.setObject(23, topology.getLastModifiedDate()); // For LocalDateTime

                    ps.addBatch();
                    batchCounter++;

                    // Execute the batch if the batch size is reached or if it's the last record
                    if (batchCounter == batchSize || i == topologies.size() - 1) {
                        int[] batchResult = ps.executeBatch();
                        connection.commit();
                        totalInserted += batchResult.length;
                        ps.clearBatch(); // Clear the batch for the next set
                        batchCounter = 0;
                    }
                }

                log.info("Total topologies inserted: {}", totalInserted);
            } catch (SQLException e) {
                connection.rollback(); // Rollback in case of error
                log.error("Error inserting topologies. Transaction rolled back.", e);
                throw e;
            }
        } catch (SQLException e) {
            log.error("Error establishing database connection.", e);
            throw e;
        }

        return totalInserted;
    }
}