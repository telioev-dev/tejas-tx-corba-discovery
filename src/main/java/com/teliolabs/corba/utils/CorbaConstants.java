package com.teliolabs.corba.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class CorbaConstants {

    private CorbaConstants() {
    }

    public static final String MANAGED_ELEMENT_STR = "ManagedElement";

    // Common Constants used in NameAndStringValue_T
    public static final String EMS_STR = "EMS";
    public static final String TIMESTAMP_SIGNATURE_STR = "LSNExt_TimestampSignature";
    public static final String MULTILAYER_SUBNETWORK_STR = "MultiLayerSubnetwork";
    public static final String FLOW_DOMAIN_STR = "FlowDomain";
    public static final String SUBNETWORK_CONNECTION_STR = "SubnetworkConnection";
    public static final String UNKNOWN_STR = "UNKNOWN";

    public static final String LOCATION_STR = "Location";

    public static final String LSN_EXT_NATIVE_LOCATION_STR = "LSNExt_NativeLocation";

    public static final String UME_IDENTIFIER = "UME";

    public static final String TOPOLOGICAL_LINK = "TopologicalLink";

    public static final String PTP = "PTP";

    public static final String CTP_STR = "CTP";

    public static final String EQUIPMENT_HOLDER_STR = "EquipmentHolder";

    public static final String LINK_RING_NAME = "LSNExt_LinkRingName";

    public static final String LINK_PROTECTION_TYPE = "LSNExt_LinkProtectionType";

    public static final String LINK_TYPE = "LSNExt_LinkType";

    public static final String LINK_TECHNOLOGY_LAYER = "LSNExt_TechnologyLayer";

    public static final String LINK_CONSISTENCY_STATE = "LSNExt_Inconsistent";

    public static final String KEY_IP_ADDRESS_VNE = "LSNExt_Comment";

    public static final String KEY_IS_DELETED = "LSNExt_IsDeleted";

    public static final String LSN_EXT_V_CAT_STR = "LSNExt_VirtualConcatRate";

    public static final Set<String> IP_ADDRESS_KEYS = new HashSet<>(Arrays.asList("LSNExt_NetworkAddress", "address", "IPAddress", "CommunicationIPAddress", "Management IP Address"));

    // FDFR

    public static final String LSN_EXT_SERVICE_NAME_STR = "LSNExt_ServiceName";
    public static final String LSN_EXT_CUSTOMER_STR = "LSNExt_Customer";
    public static final String LSN_EXT_L2VPN_ID_STR = "LSNExt_L2VPN_ID";
    public static final String LSN_EXT_TRAFFIC_ENABLED_STR = "LSNExt_Traffic_Enabled";
    public static final String LSN_EXT_VFIB_PROV_SIZE_STR = "LSNExt_VFIB_Prov_Size";
    public static final String LSN_EXT_BSC_PROFILE_STR = "LSNExt_BSC_Profile";
    public static final String LSN_EXT_TUNNEL_ID_LIST_STR = "LSNExt_TunnelID_List";
    public static final String LSN_EXT_BSC_CIR_STR = "LSNExt_BSC_CIR";
    public static final String LSN_EXT_BSC_EIR_STR = "LSNExt_BSC_EIR";






}
