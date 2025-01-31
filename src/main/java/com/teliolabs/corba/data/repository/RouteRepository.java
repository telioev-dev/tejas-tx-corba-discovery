package com.teliolabs.corba.data.repository;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.data.domain.RouteEntity;
import com.teliolabs.corba.data.queries.RouteQueries;
import com.teliolabs.corba.utils.DBUtils;
import com.teliolabs.corba.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
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
        preparedStatement.setByte(10, entity.getTupleA());
        preparedStatement.setByte(11, entity.getTupleB());
        preparedStatement.setString(12, entity.getTuple());
        preparedStatement.setTimestamp(13, Timestamp.from(ExecutionContext.getInstance().getExecutionTimestamp().toInstant()));
    }
}