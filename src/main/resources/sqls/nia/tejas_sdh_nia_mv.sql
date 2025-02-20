-- ECI_STITCHING.SDH_TOPOLOGY_ECI_ALL_REFACTORED source

CREATE MATERIALIZED VIEW "{{VIEW_NAME}}" ("USER_LABEL",
"RATE",
"TECHNOLOGY",
"A_END_NODE",
"Z_END_NODE",
"A_END_PORT",
"Z_END_PORT",
"A_END_VENDOR",
"Z_END_VENDOR",
"VENDOR",
"CIRCLE",
"SPECIFICATION",
"NMS_NAME",
"NATIVE_EMS_NAME",
"RING_NAME",
"TOPOLOGY_TYPE",
"CREATED_BY",
"CREATED_ON",
"LAST_MODIFIED")
BUILD IMMEDIATE
REFRESH COMPLETE
START WITH
TRUNC(SYSDATE) + 5 / 24
NEXT TRUNC(SYSDATE + 1) + 5 / 24
AS
SELECT
    TRIM(userLabel),
    TRIM(ethRate),
    CASE
        WHEN ethRate LIKE 'STM%' THEN 'SDH'
        WHEN ethRate LIKE 'DWDM' THEN 'DWDM'
        ELSE NULL
        END AS technology,
    TRIM(aEndNode),
    TRIM(zEndNode),
    TRIM(aEndPort),
    TRIM(Z_END_PORT_NAME),
    TRIM(aEndVendor),
    TRIM(zEndVendor),
    TRIM(VENDOR),
    TRIM(circle),
    TRIM(specification),
    TRIM(nmsName),
    TRIM(NATIVE_EMS_NAME),
    TRIM(RING_NAME),
    TRIM(TOPOLOGY_TYPE),
    TRIM(createdBy),
    TRIM(createdOn),
    CAST (LAST_MODIFIED_DATE AS TIMESTAMP WITH TIME ZONE)
FROM
    (
        SELECT
            USER_LABEL AS userLabel,
            CASE
                WHEN RATE = 19 THEN 'STM0'
                WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                WHEN RATE IN (74, 21, 26) THEN 'STM4'
                WHEN RATE IN (75, 89, 88) THEN 'STM8'
                WHEN RATE IN (76, 22, 27) THEN 'STM16'
                WHEN RATE IN (77, 28, 23) THEN 'STM64'
                WHEN RATE IN (78, 91, 90) THEN 'STM256'
                WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113, 309) THEN 'DWDM'
                ELSE TO_CHAR(RATE)
                -- Handle other cases if necessary
                END AS ethRate,
            TECHNOLOGY_LAYER AS technology,
            A_END_ME_LABEL AS aEndNode,
            Z_END_ME_LABEL AS zEndNode,
            A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
            Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS Z_END_PORT_NAME,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS AENDVENDOR,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS ZENDVENDOR,
            'ECI' AS VENDOR,
            CIRCLE AS circle,
            'SDH_TOPOLOGICAL_LINK_CON' AS specification,
            A_END_EMS AS nmsName,
            NATIVE_EMS_NAME AS NATIVE_EMS_NAME,
            RING_NAME AS RING_NAME,
            TOPOLOGY_TYPE,
            CAST(NULL AS TIMESTAMP) createdBy,
            CAST(NULL AS TIMESTAMP) createdOn,
            LAST_MODIFIED_DATE
        FROM
            ECI_STITCHING.ECI_TOPOLOGY_GJ
        WHERE
            TECHNOLOGY_LAYER IN ('SDH', 'OTN')
          AND USER_LABEL NOT LIKE '%TMU%'
          AND USER_LABEL NOT LIKE '%MSG%'
          AND IS_DELETED = 0
        UNION ALL
        SELECT
            USER_LABEL AS userLabel,
            CASE
                WHEN RATE = 19 THEN 'STM0'
                WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                WHEN RATE IN (74, 21, 26) THEN 'STM4'
                WHEN RATE IN (75, 89, 88) THEN 'STM8'
                WHEN RATE IN (76, 22, 27) THEN 'STM16'
                WHEN RATE IN (77, 28, 23) THEN 'STM64'
                WHEN RATE IN (78, 91, 90) THEN 'STM256'
                WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113, 309) THEN 'DWDM'
                ELSE TO_CHAR(RATE)
                -- Handle other cases if necessary
                END AS ethRate,
            TECHNOLOGY_LAYER AS technology,
            A_END_ME_LABEL AS aEndNode,
            Z_END_ME_LABEL AS zEndNode,
            A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
            A_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS zEndPort,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS AENDVENDOR,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS ZENDVENDOR,
            'ECI' AS VENDOR,
            CIRCLE AS circle,
            'SDH_TOPOLOGICAL_LINK_CON' AS specification,
            A_END_EMS AS nmsName,
            NATIVE_EMS_NAME AS NATIVE_EMS_NAME,
            RING_NAME AS RING_NAME,
            TOPOLOGY_TYPE,
            CAST(NULL AS TIMESTAMP) createdBy,
            CAST(NULL AS TIMESTAMP) createdOn,
            LAST_MODIFIED_DATE
        FROM
            ECI_STITCHING.ECI_TOPOLOGY_AP
        WHERE
            TECHNOLOGY_LAYER IN ('SDH', 'OTN')
          AND USER_LABEL NOT LIKE '%TMU%'
          AND USER_LABEL NOT LIKE '%MSG%'
          AND IS_DELETED = 0
        UNION ALL
        SELECT
            USER_LABEL AS userLabel,
            CASE
                WHEN RATE = 19 THEN 'STM0'
                WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                WHEN RATE IN (74, 21, 26) THEN 'STM4'
                WHEN RATE IN (75, 89, 88) THEN 'STM8'
                WHEN RATE IN (76, 22, 27) THEN 'STM16'
                WHEN RATE IN (77, 28, 23) THEN 'STM64'
                WHEN RATE IN (78, 91, 90) THEN 'STM256'
                WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113, 309) THEN 'DWDM'
                ELSE TO_CHAR(RATE)
                -- Handle other cases if necessary
                END AS ethRate,
            TECHNOLOGY_LAYER AS technology,
            A_END_ME_LABEL AS aEndNode,
            Z_END_ME_LABEL AS zEndNode,
            A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
            Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS Z_END_PORT_NAME,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS AENDVENDOR,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS ZENDVENDOR,
            'ECI' AS VENDOR,
            CIRCLE AS circle,
            'SDH_TOPOLOGICAL_LINK_CON' AS specification,
            A_END_EMS AS nmsName,
            NATIVE_EMS_NAME AS NATIVE_EMS_NAME,
            RING_NAME AS RING_NAME,
            TOPOLOGY_TYPE,
            CAST(NULL AS TIMESTAMP) createdBy,
            CAST(NULL AS TIMESTAMP) createdOn,
            LAST_MODIFIED_DATE
        FROM
            ECI_STITCHING.ECI_TOPOLOGY_DEL
        WHERE
            TECHNOLOGY_LAYER IN ('SDH', 'OTN')
          AND USER_LABEL NOT LIKE '%TMU%'
          AND USER_LABEL NOT LIKE '%MSG%'
          AND IS_DELETED = 0
        UNION ALL
        SELECT
            USER_LABEL AS userLabel,
            CASE
                WHEN RATE = 19 THEN 'STM0'
                WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                WHEN RATE IN (74, 21, 26) THEN 'STM4'
                WHEN RATE IN (75, 89, 88) THEN 'STM8'
                WHEN RATE IN (76, 22, 27) THEN 'STM16'
                WHEN RATE IN (77, 28, 23) THEN 'STM64'
                WHEN RATE IN (78, 91, 90) THEN 'STM256'
                WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113, 309) THEN 'DWDM'
                ELSE TO_CHAR(RATE)
                -- Handle other cases if necessary
                END AS ethRate,
            TECHNOLOGY_LAYER AS technology,
            A_END_ME_LABEL AS aEndNode,
            Z_END_ME_LABEL AS zEndNode,
            A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
            Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS Z_END_PORT_NAME,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS AENDVENDOR,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS ZENDVENDOR,
            'ECI' AS VENDOR,
            CIRCLE AS circle,
            'SDH_TOPOLOGICAL_LINK_CON' AS specification,
            A_END_EMS AS nmsName,
            NATIVE_EMS_NAME AS NATIVE_EMS_NAME,
            RING_NAME AS RING_NAME,
            TOPOLOGY_TYPE,
            CAST(NULL AS TIMESTAMP) createdBy,
            CAST(NULL AS TIMESTAMP) createdOn,
            LAST_MODIFIED_DATE
        FROM
            ECI_STITCHING.ECI_TOPOLOGY_KA
        WHERE
            TECHNOLOGY_LAYER IN ('SDH', 'OTN')
          AND USER_LABEL NOT LIKE '%TMU%'
          AND USER_LABEL NOT LIKE '%MSG%'
          AND IS_DELETED = 0
        UNION ALL
        SELECT
            USER_LABEL AS userLabel,
            CASE
                WHEN RATE = 19 THEN 'STM0'
                WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                WHEN RATE IN (74, 21, 26) THEN 'STM4'
                WHEN RATE IN (75, 89, 88) THEN 'STM8'
                WHEN RATE IN (76, 22, 27) THEN 'STM16'
                WHEN RATE IN (77, 28, 23) THEN 'STM64'
                WHEN RATE IN (78, 91, 90) THEN 'STM256'
                WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113, 309) THEN 'DWDM'
                ELSE TO_CHAR(RATE)
                -- Handle other cases if necessary
                END AS ethRate,
            TECHNOLOGY_LAYER AS technology,
            A_END_ME_LABEL AS aEndNode,
            Z_END_ME_LABEL AS zEndNode,
            A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
            Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS Z_END_PORT_NAME,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS AENDVENDOR,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS ZENDVENDOR,
            'ECI' AS VENDOR,
            CIRCLE AS circle,
            'SDH_TOPOLOGICAL_LINK_CON' AS specification,
            A_END_EMS AS nmsName,
            NATIVE_EMS_NAME AS NATIVE_EMS_NAME,
            RING_NAME AS RING_NAME,
            TOPOLOGY_TYPE,
            CAST(NULL AS TIMESTAMP) createdBy,
            CAST(NULL AS TIMESTAMP) createdOn,
            LAST_MODIFIED_DATE
        FROM
            ECI_STITCHING.ECI_TOPOLOGY_MUM
        WHERE
            TECHNOLOGY_LAYER IN ('SDH', 'OTN')
          AND USER_LABEL NOT LIKE '%TMU%'
          AND USER_LABEL NOT LIKE '%MSG%'
          AND IS_DELETED = 0
        UNION ALL
        SELECT
            USER_LABEL AS userLabel,
            CASE
                WHEN RATE = 19 THEN 'STM0'
                WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                WHEN RATE IN (74, 21, 26) THEN 'STM4'
                WHEN RATE IN (75, 89, 88) THEN 'STM8'
                WHEN RATE IN (76, 22, 27) THEN 'STM16'
                WHEN RATE IN (77, 28, 23) THEN 'STM64'
                WHEN RATE IN (78, 91, 90) THEN 'STM256'
                WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113, 309) THEN 'DWDM'
                ELSE TO_CHAR(RATE)
                -- Handle other cases if necessary
                END AS ethRate,
            TECHNOLOGY_LAYER AS technology,
            A_END_ME_LABEL AS aEndNode,
            Z_END_ME_LABEL AS zEndNode,
            A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
            Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS Z_END_PORT_NAME,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS AENDVENDOR,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS ZENDVENDOR,
            'ECI' AS VENDOR,
            CIRCLE AS circle,
            'SDH_TOPOLOGICAL_LINK_CON' AS specification,
            A_END_EMS AS nmsName,
            NATIVE_EMS_NAME AS NATIVE_EMS_NAME,
            RING_NAME AS RING_NAME,
            TOPOLOGY_TYPE,
            CAST(NULL AS TIMESTAMP) createdBy,
            CAST(NULL AS TIMESTAMP) createdOn,
            LAST_MODIFIED_DATE
        FROM
            ECI_STITCHING.ECI_TOPOLOGY_ROM
        WHERE
            TECHNOLOGY_LAYER IN ('SDH', 'OTN')
          AND USER_LABEL NOT LIKE '%TMU%'
          AND USER_LABEL NOT LIKE '%MSG%'
          AND IS_DELETED = 0
        UNION ALL
        SELECT
            USER_LABEL AS userLabel,
            CASE
                WHEN RATE = 19 THEN 'STM0'
                WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                WHEN RATE IN (74, 21, 26) THEN 'STM4'
                WHEN RATE IN (75, 89, 88) THEN 'STM8'
                WHEN RATE IN (76, 22, 27) THEN 'STM16'
                WHEN RATE IN (77, 28, 23) THEN 'STM64'
                WHEN RATE IN (78, 91, 90) THEN 'STM256'
                WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113, 309) THEN 'DWDM'
                ELSE TO_CHAR(RATE)
                -- Handle other cases if necessary
                END AS ethRate,
            TECHNOLOGY_LAYER AS technology,
            A_END_ME_LABEL AS aEndNode,
            Z_END_ME_LABEL AS zEndNode,
            A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
            Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS Z_END_PORT_NAME,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS AENDVENDOR,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS ZENDVENDOR,
            'ECI' AS VENDOR,
            CIRCLE AS circle,
            'SDH_TOPOLOGICAL_LINK_CON' AS specification,
            A_END_EMS AS nmsName,
            NATIVE_EMS_NAME AS NATIVE_EMS_NAME,
            RING_NAME AS RING_NAME,
            TOPOLOGY_TYPE,
            CAST(NULL AS TIMESTAMP) createdBy,
            CAST(NULL AS TIMESTAMP) createdOn,
            LAST_MODIFIED_DATE
        FROM
            ECI_STITCHING.ECI_TOPOLOGY_TN
        WHERE
            TECHNOLOGY_LAYER IN ('SDH', 'OTN')
          AND USER_LABEL NOT LIKE '%TMU%'
          AND USER_LABEL NOT LIKE '%MSG%'
          AND IS_DELETED = 0
        UNION
        SELECT
            USER_LABEL AS userLabel,
            CASE
                WHEN RATE = 19 THEN 'STM0'
                WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                WHEN RATE IN (74, 21, 26) THEN 'STM4'
                WHEN RATE IN (75, 89, 88) THEN 'STM8'
                WHEN RATE IN (76, 22, 27) THEN 'STM16'
                WHEN RATE IN (77, 28, 23) THEN 'STM64'
                WHEN RATE IN (78, 91, 90) THEN 'STM256'
                WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113, 309) THEN 'DWDM'
                ELSE TO_CHAR(RATE)
                -- Handle other cases if necessary
                END AS ethRate,
            CASE
                WHEN RATE = 19 THEN 'SDH'
                WHEN RATE IN (73, 25, 20, 93) THEN 'SDH'
                WHEN RATE IN (74, 21, 26) THEN 'SDH'
                WHEN RATE IN (75, 89, 88) THEN 'SDH'
                WHEN RATE IN (76, 22, 27) THEN 'SDH'
                WHEN RATE IN (77, 28, 23) THEN 'SDH'
                WHEN RATE IN (78, 91, 90) THEN 'SDH'
                WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113, 309) THEN 'DWDM'
                ELSE NULL
                END AS technology,
            A_END_ME_LABEL AS aEndNode,
            Z_END_ME_LABEL AS zEndNode,
            A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
            Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS Z_END_PORT_NAME,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS AENDVENDOR,
            CASE
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                WHEN A_END_PORT_NAME NOT LIKE '%UME%'
                    AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                WHEN A_END_PORT_NAME LIKE '%UME%'
                    AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                END AS ZENDVENDOR,
            'ECI' AS VENDOR,
            CIRCLE AS circle,
            'SDH_TOPOLOGICAL_LINK_CON' AS specification,
            A_END_EMS AS nmsName,
            NATIVE_EMS_NAME AS NATIVE_EMS_NAME,
            RING_NAME AS RING_NAME,
            TOPOLOGY_TYPE,
            CAST(NULL AS TIMESTAMP) createdBy,
            CAST(NULL AS TIMESTAMP) createdOn,
            LAST_MODIFIED_DATE
        FROM
            ECI_STITCHING.ECI_TOPOLOGY_APOLLO
        WHERE
            TECHNOLOGY_LAYER IN ('SDH', 'OTN')
          AND USER_LABEL NOT LIKE '%TMU%'
          AND USER_LABEL NOT LIKE '%MSG%'
          AND IS_DELETED = 0)