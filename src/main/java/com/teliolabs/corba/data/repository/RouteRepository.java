package com.teliolabs.corba.data.repository;


import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.data.domain.RouteEntity;
import com.teliolabs.corba.data.queries.RouteQueries;
import com.teliolabs.corba.utils.DBUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    protected void setPreparedStatementParameters(PreparedStatement ps, RouteEntity entity) throws SQLException {

    }
}