package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.data.domain.TopologyEntity;
import com.teliolabs.corba.data.dto.Topology;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;

public class TopologyEntityMapper implements ResultSetMapper<TopologyEntity, Topology> {

    // Singleton instance
    private static final TopologyEntityMapper INSTANCE = new TopologyEntityMapper();

    // Private constructor to enforce Singleton
    private TopologyEntityMapper() {
    }

    // Public method to get the instance
    public static TopologyEntityMapper getInstance() {
        return INSTANCE;
    }


    @Override
    public TopologyEntity mapToEntity(ResultSet resultSet) throws SQLException {
        return TopologyEntity.builder()
                .pk(resultSet.getInt("pk"))
                .tpLinkName(resultSet.getString("tp_link_name"))
                .nativeEmsName(resultSet.getString("native_ems_name"))
                .rate(resultSet.getInt("rate"))
                .linkType(resultSet.getString("link_type"))
                .direction(resultSet.getString("direction"))
                .aEndEms(resultSet.getString("a_end_ems"))
                .aEndMeName(resultSet.getString("a_end_me_name"))
                .aEndMeLabel(resultSet.getString("a_end_me_label"))
                .aEndPortName(resultSet.getString("a_end_port_name"))
                .aEndPortLabel(resultSet.getString("a_end_port_label"))
                .zEndEms(resultSet.getString("z_end_ems"))
                .zEndMeName(resultSet.getString("z_end_me_name"))
                .zEndMeLabel(resultSet.getString("z_end_me_label"))
                .zEndPortName(resultSet.getString("z_end_port_name"))
                .zEndPortLabel(resultSet.getString("z_end_port_label"))
                .userLabel(resultSet.getString("user_label"))
                .protection(resultSet.getString("protection"))
                .ringName(resultSet.getString("ring_name"))
                .inconsistent(resultSet.getString("inconsistent"))
                .technologyLayer(resultSet.getString("technology_layer"))
                .circle(resultSet.getString("circle"))
                .lastModifiedDate(resultSet.
                        getTimestamp("last_modified_date").toInstant().
                        atZone(ZoneId.systemDefault()))
                .isDeleted(resultSet.getInt("is_deleted") == 1)
                .build();
    }

    @Override
    public Topology mapToDto(ResultSet resultSet) throws SQLException {
        return Topology.builder()
                .pk(resultSet.getInt("pk"))
                .tpLinkName(resultSet.getString("tp_link_name"))
                .nativeEmsName(resultSet.getString("native_ems_name"))
                .rate(resultSet.getInt("rate"))
                .linkType(resultSet.getString("link_type"))
                .direction(resultSet.getString("direction"))
                .aEndEms(resultSet.getString("a_end_ems"))
                .aEndMeName(resultSet.getString("a_end_me_name"))
                .aEndMeLabel(resultSet.getString("a_end_me_label"))
                .aEndPortName(resultSet.getString("a_end_port_name"))
                .aEndPortLabel(resultSet.getString("a_end_port_label"))
                .zEndEms(resultSet.getString("z_end_ems"))
                .zEndMeName(resultSet.getString("z_end_me_name"))
                .zEndMeLabel(resultSet.getString("z_end_me_label"))
                .zEndPortName(resultSet.getString("z_end_port_name"))
                .zEndPortLabel(resultSet.getString("z_end_port_label"))
                .userLabel(resultSet.getString("user_label"))
                .protection(resultSet.getString("protection"))
                .ringName(resultSet.getString("ring_name"))
                .inconsistent(resultSet.getString("inconsistent"))
                .technologyLayer(resultSet.getString("technology_layer"))
                .circle(resultSet.getString("circle"))
                .lastModifiedDate(resultSet.
                        getTimestamp("last_modified_date").toInstant().
                        atZone(ZoneId.systemDefault()))
                .isDeleted(resultSet.getInt("is_deleted") == 1)
                .build();
    }
}
