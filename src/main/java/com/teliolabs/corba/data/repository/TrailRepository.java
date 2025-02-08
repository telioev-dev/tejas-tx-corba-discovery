package com.teliolabs.corba.data.repository;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.dto.Circle;
import com.teliolabs.corba.data.queries.TrailQueries;
import com.teliolabs.corba.utils.DBUtils;
import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.teliolabs.corba.utils.DBUtils.UNDERSCORE;

@Log4j2
public class TrailRepository {

    private static TrailRepository instance;

    // Public method to get the singleton instance
    public static TrailRepository getInstance() {
        if (instance == null) {
            synchronized (TrailRepository.class) {
                if (instance == null) {
                    instance = new TrailRepository();
                }
            }
        }
        return instance;
    }

    public void createEoSTrails(String sncId) {
        String circle = ExecutionContext.getInstance().getCircle().getName();
        String vendor = ExecutionContext.getInstance().getCircle().getVendor();
        String sqlFileName = "sqls/" + circle + "/SELECT_EOS_TRAIL.sql";
        ZonedDateTime executionTimestamp = ExecutionContext.getInstance().getExecutionTimestamp();
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             InputStream inputStream = TrailRepository.class.getClassLoader().getResourceAsStream(sqlFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                throw new FileNotFoundException("SQL file not found: " + sqlFileName);
            }

            // Read the entire SQL file into a single string
            String sql = reader.lines().collect(Collectors.joining("\n"));

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                // Assuming ? is meant for IN clause and sncIds is a list of strings
                pstmt.setString(1, sncId); // Set each parameter
                pstmt.setString(2, sncId); // Set each parameter
                ResultSet detailsRs = pstmt.executeQuery();
                String tableName = vendor.toUpperCase() + UNDERSCORE + "EOS" + UNDERSCORE + "TRAIL" + UNDERSCORE + circle.toUpperCase();
                String insertTrailSql = String.format(TrailQueries.INSERT_EOS_TRAIL, tableName);
                try (PreparedStatement insertStmt = connection.prepareStatement(insertTrailSql)) {
                    while (detailsRs.next()) {
                        insertStmt.setString(1, detailsRs.getString("TRAIL_ID"));
                        insertStmt.setString(2, detailsRs.getString("USER_LABEL"));
                        insertStmt.setString(4, detailsRs.getString("CIRCUIT_ID"));
                        insertStmt.setString(3, detailsRs.getString("V_CAT"));
                        insertStmt.setString(5, detailsRs.getString("SRF_ID"));
                        insertStmt.setString(6, detailsRs.getString("LAYER_RATE"));
                        insertStmt.setString(7, detailsRs.getString("RATE"));
                        insertStmt.setString(8, detailsRs.getString("TECHNOLOGY"));
                        insertStmt.setString(9, detailsRs.getString("SPECIFICATION"));
                        insertStmt.setString(10, detailsRs.getString("PATH_TYPE"));
                        insertStmt.setString(11, detailsRs.getString("TOPOLOGY"));
                        insertStmt.setString(12, detailsRs.getString("A_END_DROP_NODE"));
                        insertStmt.setString(13, detailsRs.getString("Z_END_DROP_NODE"));
                        insertStmt.setString(14, detailsRs.getString("A_END_DROP_PORT"));
                        insertStmt.setString(15, detailsRs.getString("Z_END_DROP_PORT"));
                        insertStmt.setInt(16, detailsRs.getInt("SEQUENCE"));
                        insertStmt.setString(17, detailsRs.getString("CHANNEL"));
                        insertStmt.setString(18, detailsRs.getString("A_END_NODE"));
                        insertStmt.setString(19, detailsRs.getString("Z_END_NODE"));
                        insertStmt.setString(20, detailsRs.getString("A_END_PORT"));
                        insertStmt.setString(21, detailsRs.getString("Z_END_PORT"));
                        insertStmt.setString(22, detailsRs.getString("TOPOLOGY_TYPE"));
                        insertStmt.setTimestamp(23, Timestamp.from(executionTimestamp.toInstant()));
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }

        } catch (Exception e) {
            log.error("Error executing SQL from file: {}", sqlFileName, e);
        }
    }

    public void createSDHTrails(String sncId) {
        String circle = ExecutionContext.getInstance().getCircle().getName();
        String vendor = ExecutionContext.getInstance().getCircle().getVendor();
        String sqlFileName = "sqls/" + circle + "/SELECT_SDH_TRAIL.sql";
        ZonedDateTime executionTimestamp = ExecutionContext.getInstance().getExecutionTimestamp();
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             InputStream inputStream = NiaRepository.class.getClassLoader().getResourceAsStream(sqlFileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            if (inputStream == null) {
                throw new FileNotFoundException("SQL file not found: " + sqlFileName);
            }

            // Read the entire SQL file into a single string
            String sql = reader.lines().collect(Collectors.joining("\n"));

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                // Assuming ? is meant for IN clause and sncIds is a list of strings
                pstmt.setString(1, sncId); // Set each parameter
                pstmt.setString(2, sncId); // Set each parameter
                ResultSet detailsRs = pstmt.executeQuery();
                String tableName = vendor.toUpperCase() + UNDERSCORE + "SDH" + UNDERSCORE + "TRAIL" + UNDERSCORE + circle.toUpperCase();
                String insertTrailSql = String.format(TrailQueries.INSERT_SDH_TRAIL, tableName);
                try (PreparedStatement insertStmt = connection.prepareStatement(insertTrailSql)) {
                    while (detailsRs.next()) {
                        insertStmt.setString(1, detailsRs.getString("TRAIL_ID"));
                        insertStmt.setString(2, detailsRs.getString("USER_LABEL"));
                        insertStmt.setString(3, detailsRs.getString("CIRCUIT_ID"));
                        insertStmt.setString(4, detailsRs.getString("RATE"));
                        insertStmt.setString(5, detailsRs.getString("TECHNOLOGY"));
                        insertStmt.setString(6, detailsRs.getString("SPECIFICATION"));
                        insertStmt.setString(7, detailsRs.getString("PATH_TYPE"));
                        insertStmt.setString(8, detailsRs.getString("TOPOLOGY"));
                        insertStmt.setString(9, detailsRs.getString("A_END_DROP_NODE"));
                        insertStmt.setString(10, detailsRs.getString("Z_END_DROP_NODE"));
                        insertStmt.setString(11, detailsRs.getString("A_END_DROP_PORT"));
                        insertStmt.setString(12, detailsRs.getString("Z_END_DROP_PORT"));
                        insertStmt.setInt(13, detailsRs.getInt("SEQUENCE"));
                        insertStmt.setString(14, detailsRs.getString("CHANNEL"));
                        insertStmt.setString(15, detailsRs.getString("A_END_NODE"));
                        insertStmt.setString(16, detailsRs.getString("Z_END_NODE"));
                        insertStmt.setString(17, detailsRs.getString("A_END_PORT"));
                        insertStmt.setString(18, detailsRs.getString("Z_END_PORT"));
                        insertStmt.setString(19, detailsRs.getString("TOPOLOGY_TYPE"));
                        insertStmt.setString(20, detailsRs.getString("SRF_ID"));
                        insertStmt.setTimestamp(21, Timestamp.from(executionTimestamp.toInstant()));
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }

        } catch (Exception e) {
            log.error("Error executing SQL from file: {}", sqlFileName, e);
        }
    }


    public void createSDHTrailsOld(List<String> sncIds) {
        String topologyTableName = DBUtils.getTable(DiscoveryItemType.TOPOLOGY);
        String sncTableName = DBUtils.getTable(DiscoveryItemType.SNC);
        String ptpTableName = DBUtils.getTable(DiscoveryItemType.PTP);
        String routeTableName = DBUtils.getTable(DiscoveryItemType.ROUTE);

        log.info("topologyTableName: {}, sncTableName: {}, ptpTableName: {}", topologyTableName,
                sncTableName, ptpTableName);

        String sql = String.format(TrailQueries.SELECT_SNC,
                routeTableName, //35
                ptpTableName, //36
                routeTableName, //46
                routeTableName,//63
                ptpTableName, // 64
                routeTableName, //74
                routeTableName, //91
                ptpTableName, // 92
                routeTableName, // 102
                routeTableName,
                ptpTableName,
                routeTableName,
                routeTableName,
                routeTableName,
                ptpTableName,
                ptpTableName,
                routeTableName,
                routeTableName,
                ptpTableName,
                ptpTableName,
                topologyTableName,
                sncTableName); //119

        log.info("FINAL SQL: {}", sql);
    }
}
