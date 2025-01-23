package com.teliolabs.corba.data.queries;

public final class PTPQueries {

    private PTPQueries() {
    }

    public static final String TRUNCATE_SQL = "TRUNCATE TABLE %s";

    public static final String DELETE_ALL_SQL = "DELETE FROM %s";

    public static final String SOFT_DELETE_SQL = "UPDATE %s SET is_deleted = 1 WHERE me_name = ? AND ptp_id = ?";


    public static final String INSERT_PTP_SQL =
            "INSERT INTO %s (" +
                    "ptp_id, port_location, me_name, me_label, " +
                    "product_name, port_native_name, slot, rate, type, trace_tx, trace_rx, last_modified_date) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SELECT_ALL_SQL = "SELECT * FROM %s";

}
