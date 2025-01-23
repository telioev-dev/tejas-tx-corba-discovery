package com.teliolabs.corba.utils;

import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;

public class CommonUtils {

    public static String lookupValueByName(String name, NameAndStringValue_T[] nameValues) {
        if (name == null || name.isEmpty() || nameValues == null || nameValues.length == 0) {
            return "";
        }

        for (NameAndStringValue_T value : nameValues) {
            if (value != null && name.equalsIgnoreCase(value.name)) {
                return value.value;
            }
        }
        return "";
    }
}
