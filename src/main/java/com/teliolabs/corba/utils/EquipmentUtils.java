package com.teliolabs.corba.utils;

import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;

public class EquipmentUtils {

    public static String getSlotAddress(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.EQUIPMENT_HOLDER_STR, nameValues);
    }
}
