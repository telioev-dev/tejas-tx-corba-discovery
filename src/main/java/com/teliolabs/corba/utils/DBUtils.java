package com.teliolabs.corba.utils;


import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.data.dto.Circle;
import com.teliolabs.corba.application.ExecutionContext;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class DBUtils {

    public static final String UNDERSCORE = "_";
    private static final String SEQ_SUFFIX = UNDERSCORE + "SEQ";
    private static final String TEMP_STR = "TEMP";

    public static String getSequence(DiscoveryItemType discoveryItemType) {
        Circle circle = ExecutionContext.getInstance().getCircle();
        return circle.getVendor().toUpperCase() + UNDERSCORE + discoveryItemType.name() + UNDERSCORE + circle.getName().toUpperCase() + SEQ_SUFFIX;
    }

    public static String getTable(DiscoveryItemType discoveryItemType) {
        Circle circle = ExecutionContext.getInstance().getCircle();
        return circle.getVendor().toUpperCase() + UNDERSCORE + discoveryItemType.name() + UNDERSCORE + circle.getName().toUpperCase();
    }

    public static String getTempTable(DiscoveryItemType discoveryItemType) {
        Circle circle = ExecutionContext.getInstance().getCircle();
        return circle.getVendor().toUpperCase() + UNDERSCORE + discoveryItemType.name() + UNDERSCORE + circle.getName().toUpperCase() + UNDERSCORE + TEMP_STR;
    }

    public static String getTable(String vendor, String circle, String entity) {
        return vendor.toUpperCase() + UNDERSCORE + entity + UNDERSCORE + circle.toUpperCase();
    }
}
