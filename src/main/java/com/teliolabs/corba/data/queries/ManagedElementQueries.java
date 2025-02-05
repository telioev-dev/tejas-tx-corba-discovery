package com.teliolabs.corba.data.queries;

public final class ManagedElementQueries {

    private ManagedElementQueries() {
    }


    public static final String TRUNCATE_SQL = "TRUNCATE TABLE %s";

    public static final String DELETE_ALL_SQL = "DELETE FROM %s";

    public static final String SOFT_DELETE_SQL = "UPDATE %s SET is_deleted = 1 WHERE me_name IN ";

    public static final String HARD_DELETE_SQL = "DELETE FROM %s WHERE me_name IN ";

    public static final String INSERT_SQL = "INSERT INTO %s (native_ems_name, me_name, user_label, product_name, " +
            "ip_address, software_version, location, communication_state, circle, last_modified_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SELECT_ALL_SQL = "SELECT * FROM %s";

    public static final String SELECT_ALL_NON_DELETED_SQL = "SELECT * FROM %s where is_deleted = 0";

    public static final String UPSERT_SQL =
            "MERGE INTO %s target " +
                    "USING ( " +
                    "    SELECT ? AS native_ems_name, ? AS me_name, ? AS user_label, ? AS product_name, " +
                    "           ? AS ip_address, ? AS software_version, ? AS location, ? AS communication_state,  ? AS circle, " +
                    "           ? AS last_modified_date, ? AS delta_timestamp " +
                    "    FROM dual " +
                    ") source " +
                    "ON (target.me_name = source.me_name AND target.is_deleted = 0) " +  // Match only active records
                    "WHEN MATCHED THEN " +
                    "    UPDATE SET " +
                    "        native_ems_name = source.native_ems_name, " +
                    "        user_label = source.user_label, " +
                    "        product_name = source.product_name, " +
                    "        ip_address = source.ip_address, " +
                    "        software_version = source.software_version, " +
                    "        location = source.location, " +
                    "        communication_state = source.communication_state, " +
                    "        circle = source.circle, " +
                    "        last_modified_date = source.last_modified_date " +
                    "WHEN NOT MATCHED THEN " +
                    "    INSERT (native_ems_name, me_name, user_label, product_name, ip_address, " +
                    "            software_version, location, communication_state, circle, last_modified_date) " +
                    "    VALUES (source.native_ems_name, source.me_name, source.user_label, source.product_name, " +
                    "            source.ip_address, source.software_version, source.location, source.communication_state, source.circle, " +
                    "            source.last_modified_date) " +
                    "    WHERE NOT EXISTS ( " +
                    "        SELECT 1 " +
                    "        FROM %s " +
                    "        WHERE me_name = source.me_name AND is_deleted != 0 " +  // Exclude deleted records
                    "    )";

}
