package com.teliolabs.corba.cli;

public enum CommandLineArg {
    JOB("job"),
    CIRCLE("circle"),
    ENTITY("entity"),
    TIMESTAMP("timestamp"),
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