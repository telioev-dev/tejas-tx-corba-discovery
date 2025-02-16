package com.teliolabs.corba.data.queries;

public class SNCQueries {

    public static final String SELECT_ALL_SQL = "SELECT * FROM %s";

    public static final String SELECT_ALL_PACKET_SQL = "SELECT * FROM %s where snc_rate = '309'";

    public static final String SELECT_ALL_SNC_ID_SQL = "SELECT snc_id FROM %s";

    public static final String SELECT_ALL_NON_DELETED_SQL = "SELECT * FROM %s where is_deleted = 0";

    public static final String SELECT_ALL_NON_DELETED_PACKET_SQL = "SELECT * FROM %s where snc_rate = '309' AND is_deleted = 0";

    public static final String TRUNCATE_SQL = "TRUNCATE TABLE %s";

    public static final String DELETE_ALL_SQL = "DELETE FROM %s";

    public static final String DELETE_ALL_PACKET_SQL = "DELETE FROM %s where snc_rate = '309'";

    public static final String DELETE_ALL_NON_PACKET_SQL = "DELETE FROM %s where snc_rate != '309'";

    public static final String SOFT_DELETE_SQL = "UPDATE %s SET is_deleted = 1 WHERE snc_id IN ";

    public static final String HARD_DELETE_SQL = "DELETE FROM %s WHERE snc_id IN ";

    public static final String INSERT_SQL = "INSERT INTO %s (" +
            "snc_id, snc_name, circuit_id, srf_id, snc_rate, v_cat, " +
            "a_end_me, a_end_me_label, a_end_ptp, a_end_ptp_label, a_end_channel, " +
            "z_end_me, z_end_me_label, z_end_ptp, z_end_ptp_label, z_end_channel, " +
            "last_modified_date" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String UPSERT_SQL = "MERGE INTO %s target " +
            "USING (SELECT ? AS snc_id, ? AS snc_name, ? AS circuit_id, ? AS srf_id, ? AS snc_rate, ? AS v_cat, " +
            "? AS a_end_me, ? AS a_end_me_label, ? AS a_end_ptp, ? AS a_end_ptp_label, ? AS a_end_channel, " +
            "? AS z_end_me, ? AS z_end_me_label, ? AS z_end_ptp, ? AS z_end_ptp_label, ? AS z_end_channel, " +
            "? AS last_modified_date FROM dual) source " +
            "ON (target.snc_id = source.snc_id) " +
            "WHEN MATCHED THEN " +
            "UPDATE SET snc_name = source.snc_name, " +
            "            circuit_id = source.circuit_id, " +
            "            srf_id = source.srf_id, " +
            "            snc_rate = source.snc_rate, " +
            "            v_cat = source.v_cat, " +
            "            a_end_me = source.a_end_me, " +
            "            a_end_me_label = source.a_end_me_label, " +
            "            a_end_ptp = source.a_end_ptp, " +
            "            a_end_ptp_label = source.a_end_ptp_label, " +
            "            a_end_channel = source.a_end_channel, " +
            "            z_end_me = source.z_end_me, " +
            "            z_end_me_label = source.z_end_me_label, " +
            "            z_end_ptp = source.z_end_ptp, " +
            "            z_end_ptp_label = source.z_end_ptp_label, " +
            "            z_end_channel = source.z_end_channel, " +
            "            last_modified_date = source.last_modified_date " +
            "WHEN NOT MATCHED THEN " +
            "INSERT (snc_id, snc_name, circuit_id, srf_id, snc_rate, v_cat, " +
            "        a_end_me, a_end_me_label, a_end_ptp, a_end_ptp_label, a_end_channel, " +
            "        z_end_me, z_end_me_label, z_end_ptp, z_end_ptp_label, z_end_channel, last_modified_date) " +
            "VALUES (source.snc_id, source.snc_name, source.circuit_id, source.srf_id, source.snc_rate, source.v_cat, " +
            "        source.a_end_me, source.a_end_me_label, source.a_end_ptp, source.a_end_ptp_label, source.a_end_channel, " +
            "        source.z_end_me, source.z_end_me_label, source.z_end_ptp, source.z_end_ptp_label, source.z_end_channel, " +
            "        source.last_modified_date)" +
            "    WHERE NOT EXISTS ( " +
            "        SELECT 1 " +
            "        FROM %s " +
            "        WHERE snc_id = source.snc_id AND is_deleted != 0 " +  // Exclude deleted records
            "    )";

}
