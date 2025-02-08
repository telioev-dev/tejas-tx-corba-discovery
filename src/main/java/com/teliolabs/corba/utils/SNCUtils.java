package com.teliolabs.corba.utils;

import com.teliolabs.corba.data.dto.SNC;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.multiLayerSubnetwork.MultiLayerSubnetwork_T;
import org.tmforum.mtnm.subnetworkConnection.SubnetworkConnection_T;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SNCUtils {

    private static Pattern srfIdPattern = Pattern.compile("(\\d+)(ZZ-(\\d+))?Z?");

    public static boolean isSNCDeleted(NameAndStringValue_T[] nameValues) {
        String isDeletedStr = CommonUtils.lookupValueByName(CorbaConstants.KEY_IS_DELETED, nameValues);
        return isDeletedStr != null && !isDeletedStr.isEmpty() && Boolean.parseBoolean(isDeletedStr);
    }

    public static String getSNCUserLabel(SubnetworkConnection_T snc) {
        return snc.userLabel;
    }

    public static String getSNCNativeEmsName(SubnetworkConnection_T snc) {
        return snc.nativeEMSName;
    }

    public static String getMultilayerSubnetworkName(MultiLayerSubnetwork_T mls) {
        return CommonUtils.lookupValueByName(CorbaConstants.MULTILAYER_SUBNETWORK_STR, mls.name);
    }

    public static String getSNCId(SubnetworkConnection_T snc) {
        return CommonUtils.lookupValueByName(CorbaConstants.SUBNETWORK_CONNECTION_STR, snc.name);
    }

    public static String getVCat(SubnetworkConnection_T snc) {
        String vCat = null;
        for (NameAndStringValue_T tpData : snc.additionalInfo) {
            if (tpData.name.equalsIgnoreCase(CorbaConstants.LSN_EXT_V_CAT_STR)) {
                return tpData.value;
            }
        }
        return vCat;
    }

    public static String getCircuitId(String sncName) {
        if (sncName == null || !sncName.contains("EXT")) {
            return null;
        }

        Matcher matcher = srfIdPattern.matcher(sncName);
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String group = matcher.group(i);
                if (group != null && group.length() > 8 && !group.contains("Z")) {
                    return group;
                }
            }
        }
        return null;
    }

    public static String getSRFId(String circuitId) {
        if (circuitId == null || circuitId.length() < 6) {
            return null;
        }
        return circuitId.substring(circuitId.length() - 6);
    }

    public static boolean isSDH(SNC snc) {
        return !isEoS(snc) && !isPacket(snc);
    }

    public static boolean isEoS(SNC snc) {
        short sncRate = snc.getSncRate();
        String sncId = snc.getSncId();
        String vCat = snc.getVCat();
        return !"0".equals(vCat) && sncRate != 309;
    }

    public static boolean isPacket(SNC snc) {
        short sncRate = snc.getSncRate();
        String sncId = snc.getSncId();
        return sncRate == 309 || sncId.toLowerCase().startsWith("/mpls");
    }
}
