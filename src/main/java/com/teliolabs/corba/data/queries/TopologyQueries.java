package com.teliolabs.corba.data.queries;

public final class TopologyQueries {

    private TopologyQueries() {
    }

    public static final String TRUNCATE_SQL = "TRUNCATE TABLE %s";

    public static final String DELETE_ALL_SQL = "DELETE FROM %s";

    public static final String SOFT_DELETE_SQL = "UPDATE %s SET is_deleted = 1 WHERE tp_link_name IN ";


    public static final String INSERT_SQL = "INSERT INTO %s (" +
            "tp_link_name, native_ems_name, rate, link_type, direction, " +
            "a_end_ems, a_end_me_name, a_end_me_label, a_end_port_name, a_end_port_label, " +
            "z_end_ems, z_end_me_name, z_end_me_label, z_end_port_name, z_end_port_label, " +
            "user_label, protection, ring_name, inconsistent, technology_layer, topology_type, circle, " +
            "last_modified_date" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SELECT_ALL_SQL = "SELECT * FROM %s";

    public static final String UPSERT_SQL =
            "MERGE INTO %s target " +
                    "USING ( " +
                    "    SELECT ? AS tp_link_name, ? AS native_ems_name, ? AS rate, ? AS link_type, ? AS direction, " +
                    "           ? AS a_end_ems, ? AS a_end_me_name, ? AS a_end_me_label, " +
                    "           ? AS a_end_port_name, ? AS a_end_port_label, ? AS z_end_ems, " +
                    "           ? AS z_end_me_name, ? AS z_end_me_label, ? AS z_end_port_name, " +
                    "           ? AS z_end_port_label, ? AS user_label, ? AS protection, ? AS ring_name, " +
                    "           ? AS inconsistent, ? AS technology_layer, ? AS circle, " +
                    "           ? AS last_modified_date " +
                    "    FROM dual " +
                    ") source " +
                    "ON ( " +
                    "    target.tp_link_name = source.tp_link_name AND target.is_deleted = 0 " +
                    ") " +
                    "WHEN MATCHED THEN " +
                    "    UPDATE SET " +
                    "        target.rate = source.rate, " +
                    "        target.link_type = source.link_type, " +
                    "        target.direction = source.direction, " +
                    "        target.a_end_ems = source.a_end_ems, " +
                    "        target.a_end_me_name = source.a_end_me_name, " +
                    "        target.a_end_me_label = source.a_end_me_label, " +
                    "        target.a_end_port_name = source.a_end_port_name, " +
                    "        target.a_end_port_label = source.a_end_port_label, " +
                    "        target.z_end_ems = source.z_end_ems, " +
                    "        target.z_end_me_name = source.z_end_me_name, " +
                    "        target.z_end_me_label = source.z_end_me_label, " +
                    "        target.z_end_port_name = source.z_end_port_name, " +
                    "        target.z_end_port_label = source.z_end_port_label, " +
                    "        target.protection = source.protection, " +
                    "        target.ring_name = source.ring_name, " +
                    "        target.inconsistent = source.inconsistent, " +
                    "        target.technology_layer = source.technology_layer, " +
                    "        target.circle = source.circle, " +
                    "        target.last_modified_date = source.last_modified_date " +
                    "WHEN NOT MATCHED THEN " +
                    "    INSERT ( " +
                    "        tp_link_name, native_ems_name, rate, link_type, direction, " +
                    "        a_end_ems, a_end_me_name, a_end_me_label, a_end_port_name, a_end_port_label, " +
                    "        z_end_ems, z_end_me_name, z_end_me_label, z_end_port_name, z_end_port_label, " +
                    "        user_label, protection, ring_name, inconsistent, technology_layer, circle, " +
                    "        last_modified_date" +
                    "    ) " +
                    "    VALUES ( " +
                    "        source.tp_link_name, source.native_ems_name, source.rate, source.link_type, " +
                    "        source.direction, source.a_end_ems, source.a_end_me_name, source.a_end_me_label, " +
                    "        source.a_end_port_name, source.a_end_port_label, source.z_end_ems, source.z_end_me_name, " +
                    "        source.z_end_me_label, source.z_end_port_name, source.z_end_port_label, " +
                    "        source.user_label, source.protection, source.ring_name, source.inconsistent, " +
                    "        source.technology_layer, source.circle, source.last_modified_date) " +
                    "    WHERE NOT EXISTS ( " +
                    "        SELECT 1 " +
                    "        FROM %s " +
                    "        WHERE tp_link_name = source.tp_link_name AND is_deleted != 0 " +  // Exclude deleted records
                    "    )";


}
