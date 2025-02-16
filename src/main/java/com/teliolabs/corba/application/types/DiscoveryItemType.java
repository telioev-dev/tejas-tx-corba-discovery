package com.teliolabs.corba.application.types;

public enum DiscoveryItemType {
    ALL("all"),
    ME("me"),
    PTP("ptp"),
    TOPOLOGY("topology"),
    ROUTE("route"),
    EQUIPMENT("equipment"),
    NIA_VIEW("nia_view"),
    SIA_VIEW("sia_view"),
    TRAIL("trail"),
    SNC("snc"),
    SNC_PACKET("snc_packet"),
    FDFR("fdfr"),
    ROUTE_PACKET("route_packet");


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
        return null;
    }
}