package com.teliolabs.corba.cli;

public enum CommandLineArg {
    JOB("job"),
    CIRCLE("circle"),
    ENTITY("entity"),
    ENTITY_ID("entity_id"),
    TIMESTAMP("timestamp"),
    VIEW_NAME("view_name"),
    DELTA_DAYS_BEFORE("delta_days_before"),
    VENDOR("vendor");

    private final String key;

    CommandLineArg(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}