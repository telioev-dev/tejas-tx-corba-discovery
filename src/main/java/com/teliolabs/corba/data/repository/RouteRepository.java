package com.teliolabs.corba.data.repository;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.config.DataSourceConfig;
import com.teliolabs.corba.data.domain.RouteEntity;
import com.teliolabs.corba.data.queries.EquipmentQueries;
import com.teliolabs.corba.data.queries.RouteQueries;
import com.teliolabs.corba.utils.DBUtils;
import com.teliolabs.corba.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RouteRepository extends GenericRepository<RouteEntity> {

    private static final RouteRepository INSTANCE = new RouteRepository();

    public static RouteRepository getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getTableName() {
        return DBUtils.getTable(DiscoveryItemType.ROUTE);
    }

    public int insertRoutes(List<RouteEntity> equipments, int batchSize) throws SQLException {
        return insertEntities(equipments, RouteQueries.INSERT_SQL, batchSize);
    }

    public void deleteSNCRoutes(List<String> sncRoutesToDelete) {
        if (sncRoutesToDelete == null || sncRoutesToDelete.isEmpty()) {
            log.warn("No SNCs provided for route deletion.");
            return;
        }

        String tableName = DBUtils.getTable(DiscoveryItemType.ROUTE);
        String sql = String.format(RouteQueries.DELETE_ALL_ROUTES_SNC_MULTIPLE, tableName) + "(" +
                String.join(",", Collections.nCopies(sncRoutesToDelete.size(), "?")) + ")";

        log.info("delete SNC routes SQL: {}", sql);

        try (Connection connection = DataSourceConfig.getHikariDataSource().getConnection()) {
            connection.setAutoCommit(false); // Disable auto-commit for batch processing
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                // Set the me_names to be deleted in the batch
                for (int i = 0; i < sncRoutesToDelete.size(); i++) {
                    ps.setString(i + 1, sncRoutesToDelete.get(i));
                }
                // Execute the batch update to mark records as deleted
                int rowsUpdated = ps.executeUpdate();
                log.info("Total SNC routes deleted: {}", rowsUpdated);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void setPreparedStatementParameters(PreparedStatement preparedStatement, RouteEntity entity) throws SQLException {
        preparedStatement.setString(1, StringUtils.trimString(entity.getSncId()));
        preparedStatement.setString(2, StringUtils.trimString(entity.getSncName()));
        preparedStatement.setString(3, StringUtils.trimString(entity.getAEndMe()));
        preparedStatement.setString(4, StringUtils.trimString(entity.getZEndMe()));
        preparedStatement.setString(5, StringUtils.trimString(entity.getAEndPtp()));
        preparedStatement.setString(6, StringUtils.trimString(entity.getZEndPtp()));
        preparedStatement.setString(7, StringUtils.trimString(entity.getAEndCtp()));
        preparedStatement.setString(8, StringUtils.trimString(entity.getZEndCtp()));
        preparedStatement.setString(9, StringUtils.trimString(entity.getPathType().name()));
        preparedStatement.setShort(10, entity.getTupleA());
        preparedStatement.setShort(11, entity.getTupleB());
        preparedStatement.setString(12, entity.getTuple());
        preparedStatement.setTimestamp(13, Timestamp.from(ExecutionContext.getInstance().getExecutionTimestamp().toInstant()));
    }
}