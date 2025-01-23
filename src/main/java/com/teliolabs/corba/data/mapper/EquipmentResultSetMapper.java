package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.data.domain.EquipmentEntity;
import com.teliolabs.corba.data.dto.Equipment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;

public class EquipmentResultSetMapper implements ResultSetMapper<EquipmentEntity, Equipment> {

    // Singleton instance
    private static final EquipmentResultSetMapper INSTANCE = new EquipmentResultSetMapper();

    // Private constructor to enforce Singleton
    private EquipmentResultSetMapper() {
    }

    // Public method to get the instance
    public static EquipmentResultSetMapper getInstance() {
        return INSTANCE;
    }


    @Override
    public EquipmentEntity mapToEntity(ResultSet resultSet) throws SQLException {
        return EquipmentEntity.builder()
                .pk(resultSet.getLong("pk"))
                .isDeleted(resultSet.getInt("is_deleted") == 1)
                .lastModifiedDate(resultSet.
                        getTimestamp("last_modified_date").toInstant().
                        atZone(ZoneId.systemDefault()))
                .build();
    }

    @Override
    public Equipment mapToDto(ResultSet resultSet) throws SQLException {
        return Equipment.builder()
                .isDeleted(resultSet.getInt("is_deleted") == 1)
                .lastModifiedDate(resultSet.
                        getTimestamp("last_modified_date").toInstant().
                        atZone(ZoneId.systemDefault()))
                .build();
    }
}
