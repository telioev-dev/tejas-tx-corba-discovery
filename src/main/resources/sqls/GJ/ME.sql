DROP TABLE ECI_ME_GJ;
CREATE TABLE ECI_ME_GJ
(
    pk                 NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Primary key with sequence auto-generation
    native_ems_name    VARCHAR2(255),      -- Native EMS (assumed string)
    me_name            VARCHAR2(255) CONSTRAINT gj_unique_me_name UNIQUE,      -- Managed Element name
    user_label         VARCHAR2(255),      -- User label
    product_name       VARCHAR2(255),      -- Product name
    ip_address         VARCHAR2(255),      -- IP Address
    software_version   VARCHAR2(255),      -- Software Version
    location           VARCHAR2(255),      -- Location
    communication_state           NUMBER(1,0),      -- Communication State
    circle             VARCHAR2(255) DEFAULT 'GJ',      -- Circle
    vendor             VARCHAR2(50) DEFAULT 'ECI',
    last_modified_date TIMESTAMP WITH TIME ZONE,          -- Last modified date, using timestamp for timezone support
    delta_timestamp TIMESTAMP WITH TIME ZONE,
    is_deleted         NUMBER(1,0) DEFAULT 0
);

CREATE INDEX idx_ECI_ME_GJ_native_ems_name ON ECI_ME_GJ(native_ems_name);

CREATE TABLE ECI_ME_GJ_TEMP AS SELECT * FROM ECI_ME_GJ WHERE 1=0;