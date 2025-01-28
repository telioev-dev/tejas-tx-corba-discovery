package com.teliolabs.corba.data.types;

public enum TopologyType {
    NE_NE("NE2NE"), NE_VNE("NE2VNE"), VNE_VNE("VNE2VNE");

    private final String value;

    private TopologyType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static TopologyType fromName(String name) {
        for (TopologyType b : TopologyType.values()) {
            if (b.name().equalsIgnoreCase(name)) {
                return b;
            }
        }
        throw new IllegalArgumentException(name);
    }

    public static TopologyType fromValue(String v) {
        for (TopologyType c : TopologyType.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            }
        }
        return null;
    }
}
