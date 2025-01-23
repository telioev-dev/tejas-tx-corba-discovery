package com.teliolabs.corba.data.queries;

public final class CircleQueries {

    private CircleQueries() {
    }

    public static final String SELECT_ALL_SQL = "SELECT NAME, HOST, PORT, VENDOR, EMS, EMS_VERSION, USER_NAME, PASSWORD, NAME_SERVICE, " +
            "ME_HOW_MUCH, PTP_HOW_MUCH, SNC_HOW_MUCH, TOPOLOGY_HOW_MUCH " +
            "FROM CIRCLES";

    public static final String SELECT_BY_NAME_AND_VENDOR_SQL =
            "SELECT NAME, HOST, PORT, VENDOR, EMS, EMS_VERSION, USER_NAME, PASSWORD, NAME_SERVICE, " +
                    "ME_HOW_MUCH, PTP_HOW_MUCH, SNC_HOW_MUCH, TOPOLOGY_HOW_MUCH " +
                    "FROM CIRCLES WHERE NAME = ? AND VENDOR = ?";
}
