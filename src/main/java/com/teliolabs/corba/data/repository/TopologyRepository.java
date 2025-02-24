package com.teliolabs.corba.data.repository;

import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.domain.TopologyEntity;
import com.teliolabs.corba.data.dto.Topology;
import com.teliolabs.corba.data.exception.DataAccessException;
import com.teliolabs.corba.data.mapper.TopologyResultSetMapper;
import com.teliolabs.corba.data.queries.EquipmentQueries;
import com.teliolabs.corba.data.queries.TopologyQueries;
import com.teliolabs.corba.utils.DBUtils;
import com.teliolabs.corba.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class TopologyRepository extends GenericRepository<TopologyEntity> {

    private static final TopologyRepository INSTANCE = new TopologyRepository();

    public static TopologyRepository getInstance() {
        return INSTANCE;
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
                    ps.setString(21,
                            StringUtils.trimString(
                                    topology.getTopologyType() != null ? topology.getTopologyType().value() : null));
                    ps.setString(22, topology.getCircle());
                    ps.setTimestamp(23, Timestamp.from(topology.getLastModifiedDate().toInstant()));
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

    public List<Topology> findAllTopologies() {
        return findAll(TopologyResultSetMapper.getInstance()::mapToDto, EquipmentQueries.SELECT_ALL_SQL);
    }

    private TopologyEntity mapResultSetToTopologyEntity(ResultSet resultSet) throws SQLException {
        return TopologyResultSetMapper.getInstance().mapToEntity(resultSet);
    }

    public void deleteTopologies(List<String> topologiesToDelete, boolean performHardDelete) {

        if (topologiesToDelete == null || topologiesToDelete.isEmpty()) {
            log.warn("No topologies provided for deletion.");
            return;
        }
        String tableName = DBUtils.getTable(DiscoveryItemType.TOPOLOGY);
        String placeholders = String.join(",", Collections.nCopies(topologiesToDelete.size(), "?"));
        String sql = String.format(
                performHardDelete ? TopologyQueries.HARD_DELETE_SQL : TopologyQueries.SOFT_DELETE_SQL, tableName) + "("
                + placeholders + ")";

        if (performHardDelete) {
            log.info("Performing hard delete for topologies.");
        } else {
            log.info("Performing soft delete for topologies.");
        }

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false); // Disable auto-commit for batch processing

            for (int i = 0; i < topologiesToDelete.size(); i++) {
                String tpLinkName = topologiesToDelete.get(i);
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
        String sql = String.format(TopologyQueries.UPSERT_SQL, tableName, tableName);
        int totalInserted = 0;

        // Check for duplicates before inserting
        Set<String> existingTpLinks = new HashSet<>();
        List<Topology> uniqueTopologies = new ArrayList<>();
        for (Topology topology : topologies) {
            if (existingTpLinks.contains(topology.getTpLinkName())) {
                log.warn("Duplicate topology found before insert: " + topology.getTpLinkName());
            } else {
                existingTpLinks.add(topology.getTpLinkName());
                uniqueTopologies.add(topology);
            }
        }

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                int batchCounter = 0;

                for (Topology topology : uniqueTopologies) {
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
        ps.setString(1, StringUtils.trimString(topology.getTpLinkName()));
        ps.setString(2, StringUtils.trimString(topology.getNativeEmsName()));
        ps.setInt(3, topology.getRate());
        ps.setString(4, StringUtils.trimString(topology.getLinkType()));
        ps.setString(5, StringUtils.trimString(topology.getDirection()));
        ps.setString(6, StringUtils.trimString(topology.getAEndEms()));
        ps.setString(7, StringUtils.trimString(topology.getAEndMeName()));
        ps.setString(8, StringUtils.trimString(topology.getAEndMeLabel()));
        ps.setString(9, StringUtils.trimString(topology.getAEndPortName()));
        ps.setString(10, StringUtils.trimString(topology.getAEndPortLabel()));
        ps.setString(11, StringUtils.trimString(topology.getZEndEms()));
        ps.setString(12, StringUtils.trimString(topology.getZEndMeName()));
        ps.setString(13, StringUtils.trimString(topology.getZEndMeLabel()));
        ps.setString(14, StringUtils.trimString(topology.getZEndPortName()));
        ps.setString(15, StringUtils.trimString(topology.getZEndPortLabel()));
        ps.setString(16, StringUtils.trimString(topology.getUserLabel()));
        ps.setString(17, StringUtils.trimString(topology.getProtection()));
        ps.setString(18, StringUtils.trimString(topology.getRingName()));
        ps.setString(19, StringUtils.trimString(topology.getInconsistent()));
        ps.setString(20, StringUtils.trimString(topology.getTechnologyLayer()));
        ps.setString(21,
                StringUtils.trimString(topology.getTopologyType() != null ? topology.getTopologyType().value() : null));
        ps.setString(22, StringUtils.trimString(topology.getCircle()));
        ps.setTimestamp(23, Timestamp.from(topology.getLastModifiedDate().toInstant()));
    }

    @Override
    protected String getTableName() {
        return DBUtils.getTable(DiscoveryItemType.TOPOLOGY);
    }

    @Override
    protected void setPreparedStatementParameters(PreparedStatement ps, TopologyEntity entity) throws SQLException {

    }

}