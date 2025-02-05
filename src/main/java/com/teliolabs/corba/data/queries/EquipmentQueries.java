package com.teliolabs.corba.data.queries;

public final class EquipmentQueries {

    private EquipmentQueries() {
    }


    public static final String TRUNCATE_SQL = "TRUNCATE TABLE %s";

    public static final String DELETE_ALL_SQL = "DELETE FROM %s";

    public static final String SOFT_DELETE_SQL = "UPDATE %s SET is_deleted = 1, delta_timestamp = ? WHERE me_name = ? AND location = ?";

    public static final String HARD_DELETE_SQL = "DELETE FROM WHERE me_name = ? AND location = ?";

    public static final String INSERT_SQL = "INSERT INTO %s " +
            "(me_name, me_label, user_label, software_version, serial_number, " +
            "expected_equipment, installed_equipment, location, last_modified_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SELECT_ALL_SQL = "SELECT * FROM %s";

    public static final String UPSERT_SQL =
            "MERGE INTO %s target " +
                    "USING ( " +
                    "    SELECT ? AS me_name, ? AS me_label, ? AS user_label, ? AS software_version, " +
                    "           ? AS serial_number, ? AS expected_equipment, ? AS installed_equipment, ? AS location, " +
                    "           ? AS last_modified_date, ? AS delta_timestamp " +
                    "    FROM dual " +
                    ") source " +
                    "ON (target.me_name = source.me_name AND target.location=source.location) " +  // Match only active records
                    "WHEN MATCHED THEN " +
                    "    UPDATE SET " +
                    "        me_label = source.me_label, " +
                    "        user_label = source.user_label, " +
                    "        software_version = source.software_version, " +
                    "        serial_number = source.serial_number, " +
                    "        expected_equipment = source.expected_equipment, " +
                    "        installed_equipment = source.installed_equipment, " +
                    "        last_modified_date = source.last_modified_date, " +
                    "        delta_timestamp = source.delta_timestamp, " +
                    "        is_deleted = 0 " +
                    "WHEN NOT MATCHED THEN " +
                    "    INSERT (me_name, me_label, user_label, software_version, serial_number, " +
                    "            expected_equipment, installed_equipment, location, last_modified_date, delta_timestamp, is_deleted) " +
                    "    VALUES (source.me_name, source.me_label, source.user_label, source.software_version, " +
                    "            source.serial_number, source.expected_equipment, source.installed_equipment, source.location, source.last_modified_date, " +
                    "            source.delta_timestamp, 0) " +
                    "    WHERE NOT EXISTS ( " +
                    "        SELECT 1 " +
                    "        FROM %s " +
                    "        WHERE me_name = source.me_name AND location = source.location AND is_deleted != 0 " +
                    "    )";

}
