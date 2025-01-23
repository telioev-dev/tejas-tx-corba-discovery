package com.teliolabs.corba.utils;


import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.data.dto.Circle;
import com.teliolabs.corba.application.ExecutionContext;

public final class DBUtils {

    private static final String UNDERSCORE = "_";
    private static final String SEQ_SUFFIX = UNDERSCORE + "SEQ";

    public static String getSequence(DiscoveryItemType discoveryItemType) {
        Circle circle = ExecutionContext.getInstance().getCircle();
        return circle.getVendor().toUpperCase() + UNDERSCORE + discoveryItemType.name() + UNDERSCORE + circle.getName().toUpperCase() + SEQ_SUFFIX;
    }

    public static String getTable(DiscoveryItemType discoveryItemType) {
        Circle circle = ExecutionContext.getInstance().getCircle();
        return circle.getVendor().toUpperCase() + UNDERSCORE + discoveryItemType.name() + UNDERSCORE + circle.getName().toUpperCase();
    }
}
