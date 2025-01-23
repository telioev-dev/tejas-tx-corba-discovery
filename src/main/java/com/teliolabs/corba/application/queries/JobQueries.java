package com.teliolabs.corba.application.queries;

// TODO: Add vendor, circle
public class JobQueries {

    public static final String INSERT_IMPORT_JOB_SQL =
            "INSERT INTO %s (VENDOR, CIRCLE, JOB_STATE, RUN_BY, DISCOVERY_ITEM) " +
                    "VALUES (?, ?, ?, ?, ?)";

    public static final String INSERT_DELTA_JOB_SQL =
            "INSERT INTO %s (VENDOR, CIRCLE, JOB_STATE, RUN_BY) " +
                    "VALUES (?, ?, ?)";
}
