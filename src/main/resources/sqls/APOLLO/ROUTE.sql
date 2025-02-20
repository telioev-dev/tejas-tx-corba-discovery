DROP TABLE ECI_ROUTE_APOLLO;
CREATE TABLE ECI_ROUTE_APOLLO
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
    circle             VARCHAR2(50) DEFAULT 'APOLLO',
    vendor             VARCHAR2(50) DEFAULT 'ECI',
    is_deleted         NUMBER(1,0) DEFAULT 0,
    last_modified_date TIMESTAMP WITH TIME ZONE,
    delta_timestamp    TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_ECI_ROUTE_APOLLO_snc_id ON ECI_ROUTE_APOLLO (snc_id);
CREATE INDEX idx_ECI_ROUTE_APOLLO_snc_name ON ECI_ROUTE_APOLLO (snc_name);
