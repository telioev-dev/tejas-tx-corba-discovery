-- ME --

DROP TABLE "TEJASNETWORKS_ME_MUM 1";
CREATE TABLE "TEJASNETWORKS_ME_MUM 1"
(
    pk                 NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Primary key with sequence auto-generation
    native_ems_name    VARCHAR2(255),      -- Native EMS (assumed string)
    me_name            VARCHAR2(255) CONSTRAINT MUM1_unique_me_name UNIQUE,      -- Managed Element name
    user_label         VARCHAR2(255),      -- User label
    product_name       VARCHAR2(255),      -- Product name
    ip_address         VARCHAR2(255),      -- IP Address
    software_version   VARCHAR2(255),      -- Software Version
    location           VARCHAR2(255),      -- Location
    communication_state           NUMBER(1,0),
    circle             VARCHAR2(255) DEFAULT 'MUM',      -- Circle
    vendor             VARCHAR2(50) DEFAULT 'TEJAS',
    last_modified_date TIMESTAMP,          -- Last modified date, using timestamp for timezone support
    is_deleted         NUMBER(1,0) DEFAULT 0,
    delta_timestamp  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_"TEJASNETWORKS_ME_MUM 1_native_ems_name" ON "TEJASNETWORKS_ME_MUM 1"(native_ems_name);

-- EQ --

DROP TABLE "TEJASNETWORKS_EQUIPMENT_MUM 1";
CREATE TABLE "TEJASNETWORKS_EQUIPMENT_MUM 1"
(
    pk                  NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Auto-increment
    me_name             VARCHAR2(255),
    me_label            VARCHAR2(255),
    user_label          VARCHAR2(255),
    software_version    VARCHAR2(50),
    serial_number       VARCHAR2(100),
    expected_equipment  VARCHAR2(255),
    installed_equipment VARCHAR2(255),
    location            VARCHAR2(255),
    circle             VARCHAR2(255) DEFAULT 'MUM',      -- Circle
    vendor             VARCHAR2(50) DEFAULT 'TEJAS',
    is_deleted          NUMBER(1,0) DEFAULT 0,
    last_modified_date  TIMESTAMP WITH TIME ZONE,
    delta_timestamp  TIMESTAMP WITH TIME ZONE
);

-- PTP --

DROP TABLE "TEJASNETWORKS_PTP_MUM 1";
CREATE TABLE "TEJASNETWORKS_PTP_MUM 1"
(
    pk                 NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Primary key with sequence auto-generation
    ptp_id             VARCHAR2(255),
    port_location      VARCHAR2(255),
    circle             VARCHAR2(50) DEFAULT 'MUM',
    vendor             VARCHAR2(50) DEFAULT 'TEJAS',
    me_name            VARCHAR2(255),
    me_label           VARCHAR2(255),
    product_name       VARCHAR2(255),
    trace_tx           VARCHAR2(255),
    trace_rx           VARCHAR2(255),
    port_native_name   VARCHAR2(255),
    slot               VARCHAR2(255),
    rate               VARCHAR2(50),
    type               VARCHAR2(50),
    is_deleted         NUMBER(1,0) DEFAULT 0,
    last_modified_date TIMESTAMP,
    delta_timestamp  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX "idx_TEJASNETWORKS_PTP_MUM 1_me_name" ON "TEJASNETWORKS_PTP_MUM 1"(me_name);
CREATE INDEX "idx_TEJASNETWORKS_PTP_MUM 1_me_label" ON "TEJASNETWORKS_PTP_MUM 1"(me_label);
CREATE INDEX "idx_TEJASNETWORKS_PTP_MUM 1_port_native_name" ON "TEJASNETWORKS_PTP_MUM 1"(port_native_name);
CREATE INDEX "idx_TEJASNETWORKS_PTP_MUM 1_port_location" ON "TEJASNETWORKS_PTP_MUM 1"(port_location);


-- TOPOLOGY --

DROP TABLE "TEJASNETWORKS_TOPOLODY_MUM 1";
CREATE TABLE "TEJASNETWORKS_TOPOLODY_MUM 1"
(
    pk                 NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Primary key with sequence auto-generation
    tp_link_name       VARCHAR2(400) CONSTRAINT MUM1_unique_tp_link_name UNIQUE,
    native_ems_name    VARCHAR2(400),
    rate               NUMBER,
    link_type          VARCHAR2(50),
    direction          VARCHAR2(50),
    a_end_ems          VARCHAR2(100),
    a_end_me_name      VARCHAR2(100),
    a_end_me_label     VARCHAR2(100),
    a_end_port_name    VARCHAR2(100),
    a_end_port_label   VARCHAR2(100),
    z_end_ems          VARCHAR2(100),
    z_end_me_name      VARCHAR2(100),
    z_end_me_label     VARCHAR2(100),
    z_end_port_name    VARCHAR2(100),
    z_end_port_label   VARCHAR2(100),
    user_label         VARCHAR2(400),
    protection         VARCHAR2(50),
    ring_name          VARCHAR2(300),
    inconsistent       VARCHAR2(50),
    technology_layer   VARCHAR2(50),
    topology_type      VARCHAR2(10),
    circle             VARCHAR2(50) DEFAULT 'MUM',
    vendor             VARCHAR2(50) DEFAULT 'TEJAS',
    last_modified_date TIMESTAMP WITH TIME ZONE,
    is_deleted         NUMBER(1,0) DEFAULT 0,
    delta_timestamp  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX "idx_TEJASNETWORKS_TOPOLODY_MUM 1_native_ems_name" ON "TEJASNETWORKS_TOPOLODY_MUM 1" (native_ems_name);

-- SNC --

DROP TABLE "TEJASNETWORKS_SNC_MUM 1";
CREATE TABLE "TEJASNETWORKS_SNC_MUM 1"
(
    pk                 NUMBER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Primary key with sequence auto-generation
    snc_id             VARCHAR2(255),
    snc_name           VARCHAR2(1000),
    circuit_id         NUMBER,                                              -- Integer for circuit_id
    srf_id             NUMBER,                                              -- Integer for srf_id
    snc_rate           VARCHAR2(255),
    v_cat              VARCHAR2(255),
    a_end_me           VARCHAR2(255),
    a_end_me_label     VARCHAR2(255),
    a_end_ptp          VARCHAR2(255),
    a_end_ptp_label    VARCHAR2(255),
    a_end_channel      VARCHAR2(255),
    z_end_me           VARCHAR2(255),
    z_end_me_label     VARCHAR2(255),
    z_end_ptp          VARCHAR2(255),
    z_end_ptp_label    VARCHAR2(255),
    z_end_channel      VARCHAR2(255),
    circle             VARCHAR2(50) DEFAULT 'MUM',
    vendor             VARCHAR2(50) DEFAULT 'TEJAS',
    is_deleted         NUMBER(1,0) DEFAULT 0,
    last_modified_date TIMESTAMP WITH TIME ZONE,
    delta_timestamp  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX "idx_TEJASNETWORKS_SNC_MUM 1_snc_id" ON "TEJASNETWORKS_SNC_MUM 1" (snc_id);
CREATE INDEX "idx_TEJASNETWORKS_SNC_MUM 1_snc_name" ON "TEJASNETWORKS_SNC_MUM 1" (snc_name);
CREATE INDEX "idx_TEJASNETWORKS_SNC_MUM 1_circuit_id" ON "TEJASNETWORKS_SNC_MUM 1" (circuit_id);
CREATE INDEX "idx_TEJASNETWORKS_SNC_MUM 1_srf_id" ON "TEJASNETWORKS_SNC_MUM 1" (srf_id);

-- ROUTE --

DROP TABLE "TEJASNETWORKS_ROUTE_MUM 1";
CREATE TABLE "TEJASNETWORKS_ROUTE_MUM 1"
(
    pk                 NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Primary key with sequence auto-generation
    snc_id             VARCHAR2(255),
    snc_name           VARCHAR2(1000),
    a_end_me           VARCHAR(255),
    z_end_me           VARCHAR(255),
    a_end_ptp          VARCHAR(255),
    z_end_ptp          VARCHAR(255),
    a_end_ctp          VARCHAR(255),
    z_end_ctp          VARCHAR(255),
    path_type          VARCHAR(50),
    tuple_a            NUMBER(6),
    tuple_b            NUMBER(6),
    tuple              VARCHAR(255),
    circle             VARCHAR2(50) DEFAULT 'MUM',
    vendor             VARCHAR2(50) DEFAULT 'TEJAS',
    is_deleted         NUMBER(1,0) DEFAULT 0,
    last_modified_date TIMESTAMP WITH TIME ZONE,
    delta_timestamp    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX "idx_TEJASNETWORKS_ROUTE_MUM 1_snc_id" ON "TEJASNETWORKS_ROUTE_MUM 1" (snc_id);
CREATE INDEX "idx_TEJASNETWORKS_ROUTE_MUM 1_snc_name" ON "TEJASNETWORKS_ROUTE_MUM 1" (snc_name);
