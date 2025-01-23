package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.data.domain.PTPEntity;
import com.teliolabs.corba.data.dto.PTP;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;

public class PTPResultSetMapper implements ResultSetMapper<PTPEntity, PTP> {

    // Singleton instance
    private static final PTPResultSetMapper INSTANCE = new PTPResultSetMapper();

    // Private constructor to enforce Singleton
    private PTPResultSetMapper() {
    }

    // Public method to get the instance
    public static PTPResultSetMapper getInstance() {
        return INSTANCE;
    }


    @Override
    public PTPEntity mapToEntity(ResultSet rs) throws SQLException {
        return PTPEntity.builder()
                .pk(rs.getInt("pk"))
                .ptpId(rs.getString("ptp_id"))
                .portLocation(rs.getString("port_location"))
                .circle(rs.getString("circle"))
                .vendor(rs.getString("vendor"))
                .meName(rs.getString("me_name"))
                .meLabel(rs.getString("me_label"))
                .productName(rs.getString("product_name"))
                .portNativeName(rs.getString("port_native_name"))
                .slot(rs.getString("slot"))
                .rate(rs.getString("rate"))
                .type(rs.getString("type"))
                .isDeleted(rs.getInt("is_deleted") == 1)
                .lastModifiedDate(rs.
                        getTimestamp("last_modified_date").toInstant().
                        atZone(ZoneId.systemDefault()))
                .build();
    }

    @Override
    public PTP mapToDto(ResultSet rs) throws SQLException {
        return PTP.builder()
                .ptpId(rs.getString("ptp_id"))
                .portLocation(rs.getString("port_location"))
                .circle(rs.getString("circle"))
                .vendor(rs.getString("vendor"))
                .meName(rs.getString("me_name"))
                .meLabel(rs.getString("me_label"))
                .productName(rs.getString("product_name"))
                .portNativeName(rs.getString("port_native_name"))
                .slot(rs.getString("slot"))
                .rate(rs.getString("rate"))
                .type(rs.getString("type"))
                .isDeleted(rs.getInt("is_deleted") == 1)
                .lastModifiedDate(rs.
                        getTimestamp("last_modified_date").toInstant().
                        atZone(ZoneId.systemDefault()))
                .build();
    }
}
