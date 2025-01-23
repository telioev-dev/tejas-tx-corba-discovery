package com.teliolabs.corba.data.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface ResultSetMapper<E, D> {
    E mapToEntity(ResultSet resultSet) throws SQLException;

    D mapToDto(ResultSet resultSet) throws SQLException;
}

