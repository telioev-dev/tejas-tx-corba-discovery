package com.teliolabs.corba.application.repository;

import com.teliolabs.corba.application.domain.DeltaJobEntity;
import com.teliolabs.corba.application.domain.ImportJobEntity;
import com.teliolabs.corba.application.queries.JobQueries;
import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.exception.DataAccessException;
import lombok.extern.log4j.Log4j2;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

@Log4j2
public class JobRepository {

    private static final JobRepository INSTANCE = new JobRepository();

    // Private constructor to enforce Singleton
    private JobRepository() {
    }

    // Public method to get the instance
    public static JobRepository getInstance() {
        return INSTANCE;
    }


    public ImportJobEntity insertImportJob(ImportJobEntity job) {

        String tableName = "IMPORT_JOB";
        String sql = String.format(JobQueries.INSERT_IMPORT_JOB_SQL, tableName);
        // Perform insert operation for import jobs
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, new String[]{"JOB_ID"})) {

            statement.setString(1, job.getVendor());
            statement.setString(2, job.getCircle());
            statement.setString(3, job.getJobState().name());
            statement.setString(4, job.getRunningUser());
            statement.setString(5, job.getEntity().name());

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    job.setJobId(generatedKeys.getLong(1));
                }
            }


            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    job.setJobId(generatedKeys.getLong(1));
                }
            }
            return job;
        } catch (SQLException e) {
            log.error("Error fetching all managed elements", e);
            throw new DataAccessException("Error fetching all managed elements", e);
        }
    }

    public DeltaJobEntity insertDeltaJob(DeltaJobEntity job) {
        // Perform insert operation for delta jobs
        String tableName = "DELTA_JOB";
        String sql = String.format(JobQueries.INSERT_DELTA_JOB_SQL, tableName);
        // Perform insert operation for import jobs
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, new String[]{"JOB_ID"})) {

            statement.setString(1, job.getVendor());
            statement.setString(2, job.getCircle());
            statement.setString(3, job.getJobState().name());
            statement.setString(4, job.getRunningUser());
            statement.setString(5, job.getEntity().name());


            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    job.setJobId(generatedKeys.getLong(1));
                }
            }


            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    job.setJobId(generatedKeys.getLong(1));
                }
            }
            return job;
        } catch (SQLException e) {
            log.error("Error fetching all managed elements", e);
            throw new DataAccessException("Error fetching all managed elements", e);
        }
    }

    public void updateImportJob(ImportJobEntity job) {
        // Update import job in the database
        StringBuilder sql = new StringBuilder("UPDATE IMPORT_JOB SET ");
        Map<String, Object> fieldsToUpdate = new LinkedHashMap<>();

        // Collect all non-null fields for update
        if (job.getJobState() != null) fieldsToUpdate.put("job_state", job.getJobState().name());
        if (job.getDiscoveryCount() != null) fieldsToUpdate.put("total_count", job.getDiscoveryCount());
        if (job.getEndTimestamp() != null) fieldsToUpdate.put("end_timestamp", job.getEndTimestamp());
        if (job.getErrorMessage() != null) fieldsToUpdate.put("error_details", job.getErrorMessage());
        if (job.getDuration() != null) fieldsToUpdate.put("duration", job.getDuration());
        if (job.getEntity() != null) fieldsToUpdate.put("discovery_item", job.getEntity().name());


        // Build dynamic SQL query
        for (String column : fieldsToUpdate.keySet()) {
            sql.append(column).append(" = ?, ");
        }
        sql.setLength(sql.length() - 2); // Remove trailing comma
        sql.append(" WHERE job_id = ?");

        log.info("UPDATE SQL: {}", sql.toString());
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            // Set parameter values
            int index = 1;
            for (Object value : fieldsToUpdate.values()) {
                statement.setObject(index++, value);
            }
            statement.setLong(index, job.getJobId()); // Primary key as last parameter

            // Execute update
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("No rows updated for job_id=" + job.getJobId());
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error updating job with ID: " + job.getJobId(), e);
        }
    }

    public void updateDeltaJob(DeltaJobEntity job) {
        // Update delta job in the database
        // Update import job in the database
        StringBuilder sql = new StringBuilder("UPDATE DELTA_JOB SET ");
        Map<String, Object> fieldsToUpdate = new LinkedHashMap<>();

        // Collect all non-null fields for update
        if (job.getJobState() != null) fieldsToUpdate.put("job_state", job.getJobState().name());
        if (job.getDiscoveryCount() != null) fieldsToUpdate.put("total_count", job.getDiscoveryCount());
        if (job.getDeletedCount() != null) fieldsToUpdate.put("deleted_count", job.getDeletedCount());
        if (job.getUpsertedCount() != null) fieldsToUpdate.put("upserted_count", job.getUpsertedCount());
        if (job.getEndTimestamp() != null) fieldsToUpdate.put("end_timestamp", job.getEndTimestamp());
        if (job.getErrorMessage() != null) fieldsToUpdate.put("error_details", job.getErrorMessage());
        if (job.getDuration() != null) fieldsToUpdate.put("duration", job.getDuration());
        if (job.getEntity() != null) fieldsToUpdate.put("discovery_item", job.getEntity().name());


        // Build dynamic SQL query
        for (String column : fieldsToUpdate.keySet()) {
            sql.append(column).append(" = ?, ");
        }
        sql.setLength(sql.length() - 2); // Remove trailing comma
        sql.append(" WHERE job_id = ?");

        log.info("UPDATE SQL: {}", sql.toString());
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            // Set parameter values
            int index = 1;
            for (Object value : fieldsToUpdate.values()) {
                statement.setObject(index++, value);
            }
            statement.setLong(index, job.getJobId()); // Primary key as last parameter

            // Execute update
            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("No rows updated for job_id=" + job.getJobId());
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error updating job with ID: " + job.getJobId(), e);
        }
    }

    public void updateImportJobStatus(ImportJobEntity importJobEntity) {
        String sql = "UPDATE IMPORT_JOB SET JOB_STATE = ? WHERE job_id = ?";
        executeUpdate(sql, importJobEntity.getJobState().name(), importJobEntity.getJobId());
    }

    public void updateDeltaJobStatus(DeltaJobEntity deltaJobEntity) {
        String sql = "UPDATE DELTA_JOB SET JOB_STATE = ? WHERE job_id = ?";
        executeUpdate(sql, deltaJobEntity.getJobState().name(), deltaJobEntity.getJobId());
    }

    private void executeUpdate(String sql, Object... params) {
        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated == 0) {
                throw new SQLException("No rows updated for the provided parameters");
            }

        } catch (SQLException e) {
            throw new DataAccessException("Error executing update query", e);
        }
    }
}
