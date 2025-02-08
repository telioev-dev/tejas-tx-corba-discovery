package com.teliolabs.corba.data.queries;

public final class PTPQueries {

    private PTPQueries() {
    }

    public static final String TRUNCATE_SQL = "TRUNCATE TABLE %s";

    public static final String DELETE_ALL_SQL = "DELETE FROM %s";

    public static final String SOFT_DELETE_SQL = "UPDATE %s SET is_deleted = 1 WHERE me_name = ? AND ptp_id = ?";

    public static final String HARD_DELETE_SQL = "DELETE FROM %s WHERE me_name = ? AND ptp_id = ?";


    public static final String DELETE_ALL_PTP_ME = "DELETE FROM %s WHERE me_name = ?";

    public static final String DELETE_ALL_PTP_ME_MULTIPLE = "DELETE FROM %s WHERE me_name IN";

    public static final String INSERT_PTP_SQL =
            "INSERT INTO %s (" +
                    "ptp_id, port_location, me_name, me_label, " +
                    "product_name, port_native_name, slot, rate, type, trace_tx, trace_rx, last_modified_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SELECT_ALL_SQL = "SELECT * FROM %s";

    public static final String UPSERT_SQL =
            "MERGE INTO %s target " +
                    "USING ( " +
                    "    SELECT ? AS ptp_id, ? AS port_location, ? AS me_name, ? AS me_label, " +
                    "           ? AS product_name, ? AS port_native_name, ? AS slot, ? AS rate, ? as type, ? as trace_tx, ? as trace_rx, " +
                    "           ? AS last_modified_date, ? AS delta_timestamp " +
                    "    FROM dual " +
                    ") source " +
                    "ON (target.me_name = source.me_name AND target.ptp_id=source.ptp_id) " +  // Match only active records
                    "WHEN MATCHED THEN " +
                    "    UPDATE SET " +
                    "        port_location = source.port_location, " +
                    "        me_label = source.me_label, " +
                    "        product_name = source.product_name, " +
                    "        port_native_name = source.port_native_name, " +
                    "        slot = source.slot, " +
                    "        rate = source.rate, " +
                    "        type = source.type, " +
                    "        trace_tx = source.trace_tx, " +
                    "        trace_rx = source.trace_rx, " +
                    "        last_modified_date = source.last_modified_date, " +
                    "        delta_timestamp = source.delta_timestamp, " +
                    "        is_deleted = 0 " +
                    "WHEN NOT MATCHED THEN " +
                    "    INSERT (port_location, me_label, product_name, port_native_name, slot, " +
                    "            rate, type, trace_tx, trace_rx, last_modified_date, delta_timestamp, is_deleted) " +
                    "    VALUES (source.port_location, source.me_label, source.product_name, source.port_native_name, " +
                    "            source.slot, source.rate, source.type, source.trace_tx, source.trace_rx, source.last_modified_date, " +
                    "            source.delta_timestamp, 0) " +
                    "    WHERE NOT EXISTS ( " +
                    "        SELECT 1 " +
                    "        FROM %s " +
                    "        WHERE me_name = source.me_name AND ptp_id = source.ptp_id AND is_deleted != 0 " +
                    "    )";

}
