DROP TABLE ECI_SNC_DEL;
CREATE TABLE ECI_SNC_DEL
(
    pk                 NUMBER(19) GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY, -- Primary key with sequence auto-generation
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
    circle             VARCHAR2(50) DEFAULT 'DEL',
    vendor             VARCHAR2(50) DEFAULT 'ECI',
    is_deleted         NUMBER(1,0) DEFAULT 0,
    last_modified_date TIMESTAMP WITH TIME ZONE,
    delta_timestamp  TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_ECI_SNC_DEL_snc_id ON ECI_SNC_DEL (snc_id);
CREATE INDEX idx_ECI_SNC_DEL_snc_name ON ECI_SNC_DEL (snc_name);
CREATE INDEX idx_ECI_SNC_DEL_circuit_id ON ECI_SNC_DEL (circuit_id);
CREATE INDEX idx_ECI_SNC_DEL_srf_id ON ECI_SNC_DEL (srf_id);
