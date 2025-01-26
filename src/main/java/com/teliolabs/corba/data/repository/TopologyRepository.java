package com.teliolabs.corba.data.repository;


import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.domain.TopologyEntity;
import com.teliolabs.corba.data.dto.Topology;
import com.teliolabs.corba.data.exception.DataAccessException;
import com.teliolabs.corba.data.mapper.TopologyEntityMapper;
import com.teliolabs.corba.data.queries.TopologyQueries;
import com.teliolabs.corba.utils.DBUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class TopologyRepository {

    private static final TopologyRepository INSTANCE = new TopologyRepository();

    public static TopologyRepository getInstance() {
        return INSTANCE;
    }

    public void truncate() {
        String tableName = DBUtils.getTable(DiscoveryItemType.TOPOLOGY);
        String sql = String.format(TopologyQueries.TRUNCATE_SQL, tableName);

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             Statement stmt = connection.createStatement()) {

            // Execute the TRUNCATE statement
            stmt.executeUpdate(sql);

            log.info("Table: {} truncated successfully", tableName);
        } catch (SQLException e) {
            log.error("Error truncating topologies", e);
            throw new DataAccessException("Error truncating topologies", e);
        }
    }

    public void upsertTopologies(List<Topology> topologies) throws SQLException {
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            String sequenceName = DBUtils.getSequence(DiscoveryItemType.TOPOLOGY);
            String tableName = DBUtils.getTable(DiscoveryItemType.TOPOLOGY);

            log.info("DiscoveryItemType.TOPOLOGY SequenceName: {}", sequenceName);
            log.info("DiscoveryItemType.TOPOLOGY TableName: {}", tableName);

            String sql = String.format(TopologyQueries.UPSERT_SQL, tableName, tableName);
            connection.setAutoCommit(false); // Disable auto-commit for batch processing

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int count = 0;
                for (int i = 0; i < topologies.size(); i++) {
                    Topology topology = topologies.get(i);
                    ps.setString(1, topology.getTpLinkName());
                    ps.setString(2, topology.getNativeEmsName());
                    ps.setInt(3, topology.getRate());
                    ps.setString(4, topology.getLinkType());
                    ps.setString(5, topology.getDirection());
                    ps.setString(6, topology.getAEndEms());
                    ps.setString(7, topology.getAEndMeName());
                    ps.setString(8, topology.getAEndMeLabel());
                    ps.setString(9, topology.getAEndPortName());
                    ps.setString(10, topology.getAEndPortLabel());
                    ps.setString(11, topology.getZEndEms());
                    ps.setString(12, topology.getZEndMeName());
                    ps.setString(13, topology.getZEndMeLabel());
                    ps.setString(14, topology.getZEndPortName());
                    ps.setString(15, topology.getZEndPortLabel());
                    ps.setString(16, topology.getUserLabel());
                    ps.setString(17, topology.getProtection());
                    ps.setString(18, topology.getRingName());
                    ps.setString(19, topology.getInconsistent());
                    ps.setString(20, topology.getTechnologyLayer());
                    ps.setString(21, topology.getCircle());
                    ps.setTimestamp(22, Timestamp.from(topology.getLastModifiedDate().toInstant()));
                    ps.addBatch();
                    // Execute batch after every 100 elements
                    boolean condition = (i + 1) % 100 == 0 || i == topologies.size() - 1;
                    if (condition) {
                        int[] batchResult = ps.executeBatch(); // Execute the batch
                        connection.commit();
                        count += batchResult.length;
                        ps.clearBatch(); // Clear the batch to start a new one
                    }
                }
                log.info("Total topologies upserted: {}", count);
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback(); // Rollback in case of error
                throw e;
            }
        }
    }

    public List<TopologyEntity> findAllTopologies() {
        String tableName = DBUtils.getTable(DiscoveryItemType.TOPOLOGY);
        String sql = String.format(TopologyQueries.SELECT_ALL_SQL, tableName);

        List<TopologyEntity> topologies = new ArrayList<>();

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                topologies.add(mapResultSetToTopologyEntity(resultSet));
            }
        } catch (SQLException e) {
            String errorMessage = "Error fetching all topologies from table: " + tableName;
            log.error(errorMessage, e);
            throw new DataAccessException(errorMessage, e);
        }

        return topologies;
    }


    private TopologyEntity mapResultSetToTopologyEntity(ResultSet resultSet) throws SQLException {
        return TopologyEntityMapper.getInstance().mapToEntity(resultSet);
    }

    public void deleteTopologies(List<String> topologiesToDelete) {

        if (topologiesToDelete == null || topologiesToDelete.isEmpty()) {
            log.warn("No topologies provided for deletion.");
            return;
        }
        String tableName = DBUtils.getTable(DiscoveryItemType.TOPOLOGY);
        String placeholders = String.join(",", Collections.nCopies(topologiesToDelete.size(), "?"));
        String sql = String.format(TopologyQueries.SOFT_DELETE_SQL, tableName) + "(" + placeholders + ")";

        log.info("Soft delete SQL: {}", sql);

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false); // Disable auto-commit for batch processing

            for (int i = 0; i < topologiesToDelete.size(); i++) {
                String tpLinkName = topologiesToDelete.get(i);
                log.info("tpLinkName:delete: {}", tpLinkName);
                ps.setString(i + 1, tpLinkName);
            }

            int rowsUpdated = ps.executeUpdate();
            connection.commit();
            log.info("Total topologies deleted: {}", rowsUpdated);
        } catch (SQLException e) {
            log.error("Error during topology deletion, rolling back transaction.", e);
            throw new DataAccessException("Failed to delete topologies.", e);
        }
    }

    public int insertTopologies(List<Topology> topologies, int batchSize) throws SQLException {
        if (topologies == null || topologies.isEmpty()) {
            log.warn("No topologies to insert.");
            return 0;
        }

        String tableName = DBUtils.getTable(DiscoveryItemType.TOPOLOGY);
        String sql = String.format(TopologyQueries.INSERT_SQL, tableName);

        int totalInserted = 0;

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int batchCounter = 0;

                for (Topology topology : topologies) {
                    setPreparedStatementParameters(ps, topology);
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
                log.error("Error inserting topologies. Transaction rolled back.", e);
                throw e;
            }
        } catch (SQLException e) {
            log.error("Error establishing database connection.", e);
            throw e;
        }

        return totalInserted;
    }

    private void setPreparedStatementParameters(PreparedStatement ps, Topology topology) throws SQLException {
        ps.setString(1, topology.getTpLinkName());
        ps.setString(2, topology.getNativeEmsName());
        ps.setInt(3, topology.getRate());
        ps.setString(4, topology.getLinkType());
        ps.setString(5, topology.getDirection());
        ps.setString(6, topology.getAEndEms());
        ps.setString(7, topology.getAEndMeName());
        ps.setString(8, topology.getAEndMeLabel());
        ps.setString(9, topology.getAEndPortName());
        ps.setString(10, topology.getAEndPortLabel());
        ps.setString(11, topology.getZEndEms());
        ps.setString(12, topology.getZEndMeName());
        ps.setString(13, topology.getZEndMeLabel());
        ps.setString(14, topology.getZEndPortName());
        ps.setString(15, topology.getZEndPortLabel());
        ps.setString(16, topology.getUserLabel());
        ps.setString(17, topology.getProtection());
        ps.setString(18, topology.getRingName());
        ps.setString(19, topology.getInconsistent());
        ps.setString(20, topology.getTechnologyLayer());
        ps.setString(21, topology.getCircle());
        ps.setTimestamp(22, Timestamp.from(topology.getLastModifiedDate().toInstant()));
    }

    private int executeBatch(PreparedStatement ps, Connection connection) throws SQLException {
        int[] batchResults = ps.executeBatch();
        connection.commit();
        ps.clearBatch();
        return batchResults.length;
    }
}