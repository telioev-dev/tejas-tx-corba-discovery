package com.teliolabs.corba.data.repository;


import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.data.domain.EquipmentEntity;
import com.teliolabs.corba.data.mapper.EquipmentResultSetMapper;
import com.teliolabs.corba.data.queries.EquipmentQueries;
import com.teliolabs.corba.utils.DBUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class EquipmentRepository extends GenericRepository<EquipmentEntity> {

    private static final EquipmentRepository INSTANCE = new EquipmentRepository();

    public static EquipmentRepository getInstance() {
        return INSTANCE;
    }

    public List<EquipmentEntity> findAllEquipments() {
        return findAll(EquipmentResultSetMapper.getInstance()::mapToEntity, EquipmentQueries.SELECT_ALL_SQL);
    }

    public int insertEquipments(List<EquipmentEntity> equipments, int batchSize) throws SQLException {
        return insertEntities(equipments, EquipmentQueries.DELETE_ALL_SQL, EquipmentQueries.INSERT_SQL, batchSize);
    }

    @Override
    protected String getTableName() {
        return DBUtils.getTable(DiscoveryItemType.EQUIPMENT);
    }

    @Override
    protected void setPreparedStatementParameters(PreparedStatement preparedStatement, EquipmentEntity entity) throws SQLException {
        preparedStatement.setString(1, entity.getMeName());
        preparedStatement.setString(2, entity.getMeLabel());
        preparedStatement.setString(3, entity.getUserLabel());
        preparedStatement.setString(4, entity.getSoftwareVersion());
        preparedStatement.setString(5, entity.getSerialNumber());
        preparedStatement.setString(6, entity.getExpectedEquipment());
        preparedStatement.setString(7, entity.getInstalledEquipment());
        preparedStatement.setString(8, entity.getLocation());
        preparedStatement.setTimestamp(9, Timestamp.from(entity.getLastModifiedDate().toInstant()));
    }
}