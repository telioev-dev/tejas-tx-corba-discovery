package com.teliolabs.corba.data.mapper;

import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetMapperFunction<T, R> {
    R apply(T t) throws SQLException;
}

