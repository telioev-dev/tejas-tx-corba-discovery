package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.data.dto.Circle;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CircleEntityMapper implements ResultSetMapper<Circle, Circle> {

    // Singleton instance
    private static final CircleEntityMapper INSTANCE = new CircleEntityMapper();

    // Private constructor to enforce Singleton
    private CircleEntityMapper() {
    }

    // Public method to get the instance
    public static CircleEntityMapper getInstance() {
        return INSTANCE;
    }


    @Override
    public Circle mapToEntity(ResultSet resultSet) throws SQLException {
        return getCircle(resultSet);
    }

    private Circle getCircle(ResultSet resultSet) throws SQLException {
        return Circle.builder().name(resultSet.getString("NAME")).host(resultSet.getString("HOST")).port(resultSet.getInt("PORT")).vendor(resultSet.getString("VENDOR")).ems(resultSet.getString("EMS")).emsVersion(resultSet.getString("EMS_VERSION")).userName(resultSet.getString("USER_NAME")).password(resultSet.getString("PASSWORD")).nameService(resultSet.getString("NAME_SERVICE")).meHowMuch(resultSet.getInt("ME_HOW_MUCH")).ptpHowMuch(resultSet.getInt("PTP_HOW_MUCH")).sncHowMuch(resultSet.getInt("SNC_HOW_MUCH")).topologyHowMuch(resultSet.getInt("TOPOLOGY_HOW_MUCH")).build();
    }

    @Override
    public Circle mapToDto(ResultSet resultSet) throws SQLException {
        return getCircle(resultSet);
    }
}
