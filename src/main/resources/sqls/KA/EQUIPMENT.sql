DROP TABLE ECI_EQUIPMENT_KA;
CREATE TABLE ECI_EQUIPMENT_KA
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
    circle             VARCHAR2(255) DEFAULT 'KA',      -- Circle
    vendor             VARCHAR2(50) DEFAULT 'ECI',
    is_deleted          NUMBER(1,0) DEFAULT 0,
    last_modified_date  TIMESTAMP WITH TIME ZONE,
    delta_timestamp  TIMESTAMP WITH TIME ZONE
);