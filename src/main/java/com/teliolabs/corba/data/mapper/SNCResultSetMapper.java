package com.teliolabs.corba.data.mapper;

import com.teliolabs.corba.data.domain.SNCEntity;
import com.teliolabs.corba.data.dto.SNC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;

public class SNCResultSetMapper implements ResultSetMapper<SNCEntity, SNC> {

    // Singleton instance
    private static final SNCResultSetMapper INSTANCE = new SNCResultSetMapper();

    // Private constructor to enforce Singleton
    private SNCResultSetMapper() {
    }

    // Public method to get the instance
    public static SNCResultSetMapper getInstance() {
        return INSTANCE;
    }


    @Override
    public SNCEntity mapToEntity(ResultSet resultSet) throws SQLException {
        return SNCEntity.builder().
                pk(resultSet.getInt("pk")).
                aEndChannel(resultSet.getString("a_end_channel")).
                aEndMe(resultSet.getString("a_end_me")).
                aEndMeLabel(resultSet.getString("a_end_me_label")).
                aEndPtp(resultSet.getString("a_end_ptp")).
                aEndPtpLabel(resultSet.getString("a_end_ptp_label")).
                sncId(resultSet.getString("snc_id")).
                zEndChannel(resultSet.getString("z_end_channel")).
                zEndMe(resultSet.getString("z_end_me")).
                zEndMeLabel(resultSet.getString("z_end_me")).
                zEndPtp(resultSet.getString("z_end_ptp")).
                zEndPtpLabel(resultSet.getString("z_end_ptp_label")).
                sncName(resultSet.getString("snc_name")).
                sncRate(resultSet.getShort("snc_rate")).
                circuitId(resultSet.getObject("circuit_id", Long.class)).
                vCat(resultSet.getString("v_cat")).
                srfId(resultSet.getObject("srf_id", Integer.class)).
                circle(resultSet.getString("circle")).
                isDeleted(resultSet.getInt("is_deleted") == 1).
                vendor(resultSet.getString("vendor")).
                lastModifiedDate(resultSet.
                        getTimestamp("last_modified_date").toInstant().
                        atZone(ZoneId.systemDefault())).build();
    }

    @Override
    public SNC mapToDto(ResultSet resultSet) throws SQLException {
        return SNC.builder().
                pk(resultSet.getInt("pk")).
                aEndChannel(resultSet.getString("a_end_channel")).
                aEndMe(resultSet.getString("a_end_me")).
                aEndMeLabel(resultSet.getString("a_end_me_label")).
                aEndPtp(resultSet.getString("a_end_ptp")).
                aEndPtpLabel(resultSet.getString("a_end_ptp_label")).
                sncId(resultSet.getString("snc_id")).
                zEndChannel(resultSet.getString("z_end_channel")).
                zEndMeLabel(resultSet.getString("z_end_me_label")).
                zEndPtp(resultSet.getString("z_end_ptp")).
                zEndPtpLabel(resultSet.getString("z_end_ptp_label")).
                sncName(resultSet.getString("snc_name")).
                sncRate(resultSet.getShort("snc_rate")).
                circuitId(resultSet.getObject("circuit_id", Long.class)).
                vCat(resultSet.getString("v_cat")).
                srfId(resultSet.getObject("srf_id", Integer.class)).
                circle(resultSet.getString("circle")).
                isDeleted(resultSet.getInt("is_deleted") == 1).
                vendor(resultSet.getString("vendor")).
                lastModifiedDate(resultSet.
                        getTimestamp("last_modified_date").toInstant().
                        atZone(ZoneId.systemDefault())).build();
    }
}
