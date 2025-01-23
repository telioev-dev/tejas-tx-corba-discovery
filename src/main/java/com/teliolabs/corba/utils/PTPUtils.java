package com.teliolabs.corba.utils;

import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.terminationPoint.TerminationPoint_T;
import org.tmforum.mtnm.transmissionParameters.LayeredParameters_T;

public class PTPUtils {

    public static boolean isTerminationPointDeleted(NameAndStringValue_T[] nameValues) {
        String isDeletedStr = CommonUtils.lookupValueByName(CorbaConstants.KEY_IS_DELETED, nameValues);
        return isDeletedStr != null && !isDeletedStr.isEmpty() && Boolean.parseBoolean(isDeletedStr);
    }

    public static String getPTPName(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.PTP, nameValues);
    }

    public static String getCTPName(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.CTP_STR, nameValues);
    }

    public static String getEMSName(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.EMS_STR, nameValues);
    }

    private static boolean isNullOrEmpty(NameAndStringValue_T[] array) {
        return array == null || array.length == 0;
    }

    public static String getInterfaceType(TerminationPoint_T input) {
        String portNativeEMSName = input.nativeEMSName;
        String interfaceType;
        if (portNativeEMSName.startsWith("FE") || portNativeEMSName.contains("FE-")) {
            interfaceType = "Fast Ethernet";
        } else if (portNativeEMSName.contains("STM-") || portNativeEMSName.contains("SAM_")) {
            interfaceType = "SDH";
        } else {
            interfaceType = "Electrical";
        }
        return interfaceType;
    }

    public static String getPortLocation(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LOCATION_STR, nameValues);
    }

    public static String getLSNExtNativeLocation(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LSN_EXT_NATIVE_LOCATION_STR, nameValues);
    }


    public static String deriveRate(LayeredParameters_T layeredParameter) {
        return layeredParameter.layer + " - " + RateUtil.getRate(layeredParameter.layer);
    }
}
