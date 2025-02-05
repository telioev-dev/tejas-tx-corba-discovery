package com.teliolabs.corba.application.types;

public enum DbProfile {
    DEV, STG, PROD;

    public static DbProfile fromName(String name) {
        for (DbProfile b : DbProfile.values()) {
            if (b.name().equalsIgnoreCase(name)) {
                return b;
            }
        }
        return null;
    }
}
