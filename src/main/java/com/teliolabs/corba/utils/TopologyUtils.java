package com.teliolabs.corba.utils;

import com.teliolabs.corba.data.domain.TopologyEntity;
import com.teliolabs.corba.data.dto.Topology;
import com.teliolabs.corba.data.types.TopologyType;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.topologicalLink.TopologicalLink_T;

public class TopologyUtils {

    public static void deriveTopologyType(Topology topology) {
        if (topology.getAEndMeName().contains(ApplicationConstants.UME_STR) && topology.getZEndMeName().contains(ApplicationConstants.UME_STR)) {
            topology.setTopologyType(TopologyType.VNE_VNE);
        } else if (!topology.getAEndMeName().contains(ApplicationConstants.UME_STR) && !topology.getZEndMeName().contains(ApplicationConstants.UME_STR)) {
            topology.setTopologyType(TopologyType.NE_NE);
        } else if (topology.getAEndMeName().contains(ApplicationConstants.UME_STR) || topology.getZEndMeName().contains(ApplicationConstants.UME_STR)) {
            topology.setTopologyType(TopologyType.NE_VNE);
            VendorUtils.processTopology(topology);
        }
    }

    public static TopologyType deriveTopologyType(TopologyEntity topology) {
        if (topology.getAEndMeName().contains(ApplicationConstants.UME_STR) && topology.getZEndMeName().contains(ApplicationConstants.UME_STR)) {
            return TopologyType.VNE_VNE;
        } else if (!topology.getAEndMeName().contains(ApplicationConstants.UME_STR) && !topology.getZEndMeName().contains(ApplicationConstants.UME_STR)) {
            return TopologyType.NE_NE;
        } else if (topology.getAEndMeName().contains(ApplicationConstants.UME_STR) || topology.getZEndMeName().contains(ApplicationConstants.UME_STR)) {
            return TopologyType.NE_VNE;
        }
        return null;
    }

    public static boolean isTopologyDeleted(NameAndStringValue_T[] nameValues) {
        String isDeletedStr = CommonUtils.lookupValueByName(CorbaConstants.KEY_IS_DELETED, nameValues);
        return isDeletedStr != null && !isDeletedStr.isEmpty() && Boolean.parseBoolean(isDeletedStr);
    }

    public static String getLinkProtectionType(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LINK_PROTECTION_TYPE, nameValues);
    }

    public static String getLinkRingName(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LINK_RING_NAME, nameValues);
    }

    public static String getLinkType(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LINK_TYPE, nameValues);
    }

    public static String getTechnologyLayer(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LINK_TECHNOLOGY_LAYER, nameValues);
    }

    public static String getInconsistent(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.LINK_CONSISTENCY_STATE, nameValues);
    }

    public static String getLinkDirection(TopologicalLink_T topology) {
        if (topology == null || topology.direction == null) {
            return ApplicationConstants.EMPTY_STR;
        }

        switch (topology.direction.value()) {
            case 0:
                return "UNKNOWN/UNI-DIRECTIONAL";
            case 1:
                return "NON_REVERTIVE/BI-DIRECTIONAL";
            case 2:
                return "REVERTIVE";
            default:
                return ApplicationConstants.EMPTY_STR;
        }
    }

    public static String getLinkName(NameAndStringValue_T[] nameValues) {
        return CommonUtils.lookupValueByName(CorbaConstants.TOPOLOGICAL_LINK, nameValues);
    }

    private static boolean isNullOrEmpty(NameAndStringValue_T[] array) {
        return array == null || array.length == 0;
    }
}
