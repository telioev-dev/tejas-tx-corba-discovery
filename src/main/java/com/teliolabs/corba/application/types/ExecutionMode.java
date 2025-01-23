package com.teliolabs.corba.application.types;

import lombok.extern.log4j.Log4j2;

@Log4j2
public enum ExecutionMode {
    DELTA("delta"), IMPORT("import"), STANDALONE("standalone");

    private final String value;

    private ExecutionMode(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }


    public static ExecutionMode fromName(String name) {
        for (ExecutionMode b : ExecutionMode.values()) {
            log.info("Name: {}, Value: {}", b.name(), b.value);
            if (b.name().equalsIgnoreCase(name)) {
                return b;
            }
        }
        return null;
    }

    public static ExecutionMode fromValue(String name) {
        for (ExecutionMode b : ExecutionMode.values()) {
            log.info("Name: {}, Value: {}", b.name(), b.value);
            if (b.value.equalsIgnoreCase(name)) {
                return b;
            }
        }
        return null;
    }
}
