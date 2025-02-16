package com.teliolabs.corba.utils;

import org.tmforum.mtnm.flowDomainFragment.FlowDomainFragment_T;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;

public class FDFRUtils {

    public static String getFDFRType(FlowDomainFragment_T flowDomainFragmentT) {
        if (flowDomainFragmentT.fdfrType == null) {
            return null;
        }

        String type = flowDomainFragmentT.fdfrType;

        if (type.contains("FDFRT_MULTIPOINT")) {
            return "MP2MP";
        } else if (type.contains("FDFRT_POINT_TO_MULTIPOINT")) {
            return "P2MP";
        } else if (type.contains("FDFRT_POINT_TO_POINT")) {
            return "P2P";
        }

        return null;
    }

    public static String getServiceName(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LSN_EXT_SERVICE_NAME_STR, nameValues);
    }

    public static String getCustomer(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LSN_EXT_CUSTOMER_STR, nameValues);
    }

    public static String getL2VPNId(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LSN_EXT_L2VPN_ID_STR, nameValues);
    }

    public static String getTrafficEnabled(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LSN_EXT_TRAFFIC_ENABLED_STR, nameValues);
    }

    public static String getVFIBProvSize(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LSN_EXT_VFIB_PROV_SIZE_STR, nameValues);
    }

    public static String getBscProfile(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LSN_EXT_BSC_PROFILE_STR, nameValues);
    }

    public static String getTunnelIdList(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LSN_EXT_TUNNEL_ID_LIST_STR, nameValues);
    }

    public static String getBscCir(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LSN_EXT_BSC_CIR_STR, nameValues);
    }

    public static String getBscEir(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LSN_EXT_BSC_EIR_STR, nameValues);
    }

}
