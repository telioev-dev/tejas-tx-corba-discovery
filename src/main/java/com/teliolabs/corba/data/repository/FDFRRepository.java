package com.teliolabs.corba.data.repository;


import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.data.domain.FDFREntity;
import com.teliolabs.corba.utils.DBUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Log4j2
public class FDFRRepository extends GenericRepository<FDFREntity> {

    private static final FDFRRepository INSTANCE = new FDFRRepository();

    public static FDFRRepository getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getTableName() {
        return DBUtils.getTable(DiscoveryItemType.FDFR);
    }

    @Override
    protected void setPreparedStatementParameters(PreparedStatement ps, FDFREntity entity) throws SQLException {

    }
}