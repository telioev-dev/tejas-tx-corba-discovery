package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.data.domain.ManagedElementEntity;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.types.CommunicationState;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;

public class ManagedElementResultSetMapper implements ResultSetMapper<ManagedElementEntity, ManagedElement> {

    // Singleton instance
    private static final ManagedElementResultSetMapper INSTANCE = new ManagedElementResultSetMapper();

    // Private constructor to enforce Singleton
    private ManagedElementResultSetMapper() {
    }

    // Public method to get the instance
    public static ManagedElementResultSetMapper getInstance() {
        return INSTANCE;
    }


    @Override
    public ManagedElementEntity mapToEntity(ResultSet resultSet) throws SQLException {
        return ManagedElementEntity.builder()
                .pk(resultSet.getInt("pk"))
                .nativeEmsName(resultSet.getString("native_ems_name"))
                .meName(resultSet.getString("me_name"))
                .userLabel(resultSet.getString("user_label"))
                .productName(resultSet.getString("product_name"))
                .ipAddress(resultSet.getString("ip_address"))
                .softwareVersion(resultSet.getString("software_version"))
                .location(resultSet.getString("location"))
                .communicationState(CommunicationState.fromState(resultSet.getInt("communication_state")))
                .circle(resultSet.getString("circle"))
                .vendor(resultSet.getString("vendor"))
                .isDeleted(resultSet.getInt("is_deleted") == 1)
                .lastModifiedDate(resultSet.
                        getTimestamp("last_modified_date").toInstant().
                        atZone(ZoneId.systemDefault()))
                .build();
    }

    @Override
    public ManagedElement mapToDto(ResultSet resultSet) throws SQLException {
        return ManagedElement.builder()
                .pk(resultSet.getInt("pk"))
                .nativeEmsName(resultSet.getString("native_ems_name"))
                .meName(resultSet.getString("me_name"))
                .userLabel(resultSet.getString("user_label"))
                .productName(resultSet.getString("product_name"))
                .ipAddress(resultSet.getString("ip_address"))
                .softwareVersion(resultSet.getString("software_version"))
                .location(resultSet.getString("location"))
                .communicationState(CommunicationState.fromState(resultSet.getInt("communication_state")))
                .circle(resultSet.getString("circle"))
                .vendor(resultSet.getString("vendor"))
                .isDeleted(resultSet.getInt("is_deleted") == 1)
                .lastModifiedDate(resultSet.
                        getTimestamp("last_modified_date").toInstant().
                        atZone(ZoneId.systemDefault()))
                .build();
    }
}
