DROP TABLE ECI_FDFR_GJ;

CREATE TABLE ECI_FDFR_GJ
(
    SERVICE_NAME           VARCHAR2(4000),
    SERVICE_TYPE           VARCHAR2(4000),
    SERVICE_ID             VARCHAR2(4000),
    RATE                   VARCHAR2(500),
    CUSTOMER               VARCHAR2(4000),
    SERVICE_END_A_POINT    VARCHAR2(4000),
    SERVICE_END_Z_POINT    VARCHAR2(4000),
    C_VLAN                 VARCHAR2(1000),
    S_VLAN                 VARCHAR2(1000),
    SERVICE_CIR_MB         VARCHAR2(1000),
    SERVICE_EIR_MB         VARCHAR2(1000),
    BANDWIDTH_PROFILE_NAME VARCHAR2(2000),
    FDFR_STATE             VARCHAR2(1000),
    FDFR_DIRECTION         VARCHAR2(1000),
    L2VPN_ID               VARCHAR2(1000),
    TUNNEL_ID_LIST         VARCHAR2(2000),
    circle                 VARCHAR2(255) DEFAULT 'GJ', -- Circle
    vendor                 VARCHAR2(50) DEFAULT 'ECI',
    last_modified_date     TIMESTAMP WITH TIME ZONE,   -- Last modified date, using timestamp for timezone support
    delta_timestamp        TIMESTAMP WITH TIME ZONE
);