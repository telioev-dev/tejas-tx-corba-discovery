DROP TABLE ECI_EOS_TRAIL_ROM;

CREATE TABLE ECI_EOS_TRAIL_ROM
(
    "CIRCLE"             VARCHAR2(200) DEFAULT 'ROM',
    "TRAIL_ID"           VARCHAR2(2000),
    "V_CAT"               VARCHAR2(200),
    "USER_LABEL"         VARCHAR2(4000),
    "CIRCUIT_ID"         VARCHAR2(300),
    "LAYER_RATE"         VARCHAR2(4000),
    "RATE"               VARCHAR2(260),
    "SRF_ID"             VARCHAR2(500),
    "TECHNOLOGY"         VARCHAR2(500),
    "SPECIFICATION"      VARCHAR2(2600),
    "PATH_TYPE"          VARCHAR2(260),
    "VENDOR"             VARCHAR2(26) DEFAULT 'ECI',
    "TOPOLOGY"           VARCHAR2(4000),
    "A_END_DROP_NODE"    VARCHAR2(2600),
    "Z_END_DROP_NODE"    VARCHAR2(2600),
    "A_END_DROP_PORT"    VARCHAR2(2600),
    "Z_END_DROP_PORT"    VARCHAR2(2600),
    "SEQUENCE"           VARCHAR2(300),
    "CHANNEL"            VARCHAR2(2600),
    "A_END_NODE"         VARCHAR2(2600),
    "Z_END_NODE"         VARCHAR2(2600),
    "A_END_PORT"         VARCHAR2(2600),
    "Z_END_PORT"         VARCHAR2(2600),
    "TOPOLOGY_TYPE"      VARCHAR2(2000),
    "LAST_MODIFIED_TIME" TIMESTAMP WITH TIME ZONE
);