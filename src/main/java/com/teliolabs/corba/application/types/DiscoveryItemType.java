package com.teliolabs.corba.application.types;

public enum DiscoveryItemType {
    ALL("all"),
    ME("me"),
    PTP("ptp"),
    TOPOLOGY("topology"),
    ROUTE("route"),
    EQUIPMENT("equipment"),
    SNC("snc");


    private final String value;

    private DiscoveryItemType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static DiscoveryItemType fromName(String name) {
        for (DiscoveryItemType b : DiscoveryItemType.values()) {
            if (b.name().equalsIgnoreCase(name)) {
                return b;
            }
        }
        throw new IllegalArgumentException(name);
    }

    public static DiscoveryItemType fromValue(String v) {
        for (DiscoveryItemType c : DiscoveryItemType.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            }
        }
        return DiscoveryItemType.ALL;
    }
}