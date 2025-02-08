package com.teliolabs.corba.data.repository;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.domain.EquipmentEntity;
import com.teliolabs.corba.data.dto.Equipment;
import com.teliolabs.corba.data.exception.DataAccessException;
import com.teliolabs.corba.data.mapper.EquipmentResultSetMapper;
import com.teliolabs.corba.data.queries.EquipmentQueries;
import com.teliolabs.corba.data.queries.ManagedElementQueries;
import com.teliolabs.corba.utils.DBUtils;
import com.teliolabs.corba.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class EquipmentRepository extends GenericRepository<EquipmentEntity> {

    private static final int ORACLE_IN_CLAUSE_LIMIT = 300;

    private static final EquipmentRepository INSTANCE = new EquipmentRepository();

    public static EquipmentRepository getInstance() {
        return INSTANCE;
    }

    public List<EquipmentEntity> findAllEquipments() {
        return findAll(EquipmentResultSetMapper.getInstance()::mapToEntity, EquipmentQueries.SELECT_ALL_SQL);
    }

    public int insertEquipments(List<EquipmentEntity> equipments, int batchSize) throws SQLException {
        return insertEntities(equipments, EquipmentQueries.INSERT_SQL, batchSize);
    }

    public void deleteManagedElementEquipments(List<String> meEquipmentsToDelete) {
        if (meEquipmentsToDelete == null || meEquipmentsToDelete.isEmpty()) {
            log.warn("No MEs provided for equipment deletion.");
            return;
        }

        if (meEquipmentsToDelete.size() <= ORACLE_IN_CLAUSE_LIMIT) {
            // Run a single query if size is ≤ 1000
            deleteInBatch(meEquipmentsToDelete);
        }
        for (int i = 0; i < meEquipmentsToDelete.size(); i += ORACLE_IN_CLAUSE_LIMIT) {
            List<String> batch = meEquipmentsToDelete.subList(i, Math.min(i + ORACLE_IN_CLAUSE_LIMIT, meEquipmentsToDelete.size()));
            deleteInBatch(batch);
        }
    }

    private void deleteInBatch(List<String> meEquipmentsToDelete) {
        String tableName = DBUtils.getTable(DiscoveryItemType.EQUIPMENT);
        String sql = String.format(EquipmentQueries.DELETE_ALL_EQ_ME_MULTIPLE, tableName) + "(" +
                String.join(",", Collections.nCopies(meEquipmentsToDelete.size(), "?")) + ")";

        log.info("delete EQ SQL: {}", sql);

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit for batch processing
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                // Set the me_names to be deleted in the batch
                for (int i = 0; i < meEquipmentsToDelete.size(); i++) {
                    ps.setString(i + 1, meEquipmentsToDelete.get(i));
                }
                // Execute the batch update to mark records as deleted
                int rowsUpdated = ps.executeUpdate();
                log.info("Total ME EQs deleted: {}", rowsUpdated);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteEquipments(List<Equipment> equipmentsToDelete, boolean performHardDelete) {

        if (equipmentsToDelete == null || equipmentsToDelete.isEmpty()) {
            log.warn("No Equipments provided for deletion.");
            return;
        }

        String sql = String.format(performHardDelete ? EquipmentQueries.HARD_DELETE_SQL : EquipmentQueries.SOFT_DELETE_SQL, getTableName());
        if (performHardDelete) {
            log.info("EQ Hard delete SQL: {}", sql);
        } else {
            log.info("EQ Soft delete SQL: {}", sql);
        }


        int batchSize = 50;
        int totalDeleted = 0;
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            connection.setAutoCommit(false); // Disable auto-commit for batch processing
            int batchCounter = 0;
            for (int i = 0; i < equipmentsToDelete.size(); i++) {
                Equipment equipment = equipmentsToDelete.get(i);
                ps.setTimestamp(1, Timestamp.from(equipment.getDeltaTimestamp().toInstant()));
                ps.setString(2, equipment.getMeName());
                ps.setString(3, equipment.getLocation());
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
            log.info("Total EQs deleted: {}", totalDeleted);
        } catch (SQLException e) {
            log.error("Error during EQ deletion, rolling back transaction.", e);
            throw new DataAccessException("Failed to delete EQs.", e);
        }
    }

    @Override
    protected String getTableName() {
        return DBUtils.getTable(DiscoveryItemType.EQUIPMENT);
    }

    @Override
    protected void setPreparedStatementParameters(PreparedStatement preparedStatement, EquipmentEntity entity) throws SQLException {
        preparedStatement.setString(1, StringUtils.trimString(entity.getMeName()));
        preparedStatement.setString(2, StringUtils.trimString(entity.getMeLabel()));
        preparedStatement.setString(3, StringUtils.trimString(entity.getUserLabel()));
        preparedStatement.setString(4, StringUtils.trimString(entity.getSoftwareVersion()));
        preparedStatement.setString(5, StringUtils.trimString(entity.getSerialNumber()));
        preparedStatement.setString(6, StringUtils.trimString(entity.getExpectedEquipment()));
        preparedStatement.setString(7, StringUtils.trimString(entity.getInstalledEquipment()));
        preparedStatement.setString(8, StringUtils.trimString(entity.getLocation()));
        preparedStatement.setTimestamp(9, Timestamp.from(entity.getLastModifiedDate().toInstant()));
    }

    private void setPreparedStatementParameters(PreparedStatement preparedStatement, Equipment entity) throws SQLException {
        ZonedDateTime executionTimestamp = ExecutionContext.getInstance().getExecutionTimestamp();
        preparedStatement.setString(1, StringUtils.trimString(entity.getMeName()));
        preparedStatement.setString(2, StringUtils.trimString(entity.getMeLabel()));
        preparedStatement.setString(3, StringUtils.trimString(entity.getUserLabel()));
        preparedStatement.setString(4, StringUtils.trimString(entity.getSoftwareVersion()));
        preparedStatement.setString(5, StringUtils.trimString(entity.getSerialNumber()));
        preparedStatement.setString(6, StringUtils.trimString(entity.getExpectedEquipment()));
        preparedStatement.setString(7, StringUtils.trimString(entity.getInstalledEquipment()));
        preparedStatement.setString(8, StringUtils.trimString(entity.getLocation()));
        preparedStatement.setTimestamp(9, Timestamp.from(entity.getLastModifiedDate().toInstant()));
        preparedStatement.setTimestamp(10, Timestamp.from(executionTimestamp.toInstant()));
    }

    public int upsertEquipments(List<Equipment> equipments, int batchSize) throws SQLException {
        if (equipments == null || equipments.isEmpty()) {
            return 0;
        }

        String upsertSQL = String.format(EquipmentQueries.UPSERT_SQL, getTableName(), getTableName());
        log.info("upsertSQL: {}", upsertSQL);
        int totalInserted = 0;

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement ps = connection.prepareStatement(upsertSQL)) {
                int batchCounter = 0;

                for (Equipment entity : equipments) {
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
}