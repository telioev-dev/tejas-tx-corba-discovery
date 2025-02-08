package com.teliolabs.corba.application.types;

import lombok.extern.log4j.Log4j2;

@Log4j2
public enum DbProfile {
    DEV, STG, PROD;

    public static DbProfile fromName(String name) {
        for (DbProfile b : DbProfile.values()) {
            log.info("Name: {}", b.name());
            if (b.name().equalsIgnoreCase(name)) {
                return b;
            }
        }
        return null;
    }
}
