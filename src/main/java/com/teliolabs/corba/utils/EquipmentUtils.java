package com.teliolabs.corba.utils;

import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;

public class EquipmentUtils {

    public static String getSlotAddress(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.EQUIPMENT_HOLDER_STR, nameValues);
    }

    public static boolean isEquipmentDeleted(NameAndStringValue_T[] nameValues) {
        String isDeletedStr = CommonUtils.lookupValueByName(CorbaConstants.KEY_IS_DELETED, nameValues);
        return isDeletedStr != null && !isDeletedStr.isEmpty() && Boolean.parseBoolean(isDeletedStr);
    }
}
