package com.teliolabs.corba.utils;


import com.teliolabs.corba.data.dto.ManagedElement;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.managedElement.ManagedElement_T;

public class ManagedElementUtils {

    public static String getMEName(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.MANAGED_ELEMENT_STR, nameValues);
    }

    public static boolean isUME(ManagedElement_T managedElement) {
        String meName = getMEName(managedElement.name);
        return meName.contains(CorbaConstants.UME_IDENTIFIER);
    }

    public static boolean isUME(String meName) {
        return meName != null && meName.contains(CorbaConstants.UME_IDENTIFIER);
    }

    public static String getIPAddress(ManagedElement_T managedElement) {
        if (managedElement == null) {
            return ApplicationConstants.EMPTY_STR;
        }

        return getIPAddress(getMEName(managedElement.name), managedElement.additionalInfo);
    }

    public static String getProductName(ManagedElement managedElement) {
        return managedElement != null ? managedElement.getProductName() : null;
    }

    public static String getMeLabel(ManagedElement managedElement) {
        return managedElement != null ? managedElement.getNativeEmsName() : null;
    }

    public static String getIPAddress(String meName, NameAndStringValue_T[] additionalInfo) {
        if (isNullOrEmpty(additionalInfo)) {
            return ApplicationConstants.EMPTY_STR;
        }

        boolean isUME = isUME(meName);
        return isUME ? getIPAddressForUME(additionalInfo) : getIPAddressFromKeys(additionalInfo);
    }

    public static String getIPAddressForUME(NameAndStringValue_T[] additionalInfo) {
        return CommonUtils.lookupValueByName(CorbaConstants.KEY_IP_ADDRESS_VNE, additionalInfo);
    }

    private static String getIPAddressFromKeys(NameAndStringValue_T[] additionalInfo) {
        for (NameAndStringValue_T nv : additionalInfo) {
            if (CorbaConstants.IP_ADDRESS_KEYS.contains(nv.name)) {
                return nv.value;
            }
        }
        return ApplicationConstants.EMPTY_STR;
    }

    private static boolean isNullOrEmpty(NameAndStringValue_T[] array) {
        return array == null || array.length == 0;
    }
}
