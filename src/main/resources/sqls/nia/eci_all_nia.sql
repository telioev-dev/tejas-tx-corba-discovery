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
    TRIM(userLabel) AS USER_LABEL ,
    TRIM(ethRate) AS RATE,
    CASE
        WHEN ethRate LIKE 'STM%' THEN 'SDH'
        WHEN ethRate LIKE 'DWDM' THEN 'DWDM'
        WHEN ethRate LIKE '%GigE%' THEN 'ETHERNET'
        ELSE NULL
        END AS technology,
    TRIM(aEndNode) AS A_END_NODE,
    TRIM(zEndNode) AS Z_END_NODE,
    TRIM(aEndPort) AS A_END_PORT,
    TRIM(zEndPort) AS Z_END_PORT,
    TRIM(aEndVendor) AS A_END_VENDOR,
    TRIM(zEndVendor) AS Z_END_VENDOR,
    TRIM(VENDOR) AS VENDOR,
    TRIM(circle) AS CIRCLE,
    TRIM(specification) AS SPECIFICATION,
    TRIM(nmsName) AS NMS_NAME,
    TRIM(NATIVE_EMS_NAME) AS NATIVE_EMS_NAME,
    TRIM(RING_NAME) AS RING_NAME,
    TRIM(TOPOLOGY_TYPE) AS TOPOLOGY_TYPE,
    TRIM(CREATED_BY) AS CREATED_BY,
    TRIM(CREATED_ON) AS CREATED_ON ,
    CAST(LAST_MODIFIED_DATE AS TIMESTAMP WITH TIME ZONE) AS LAST_MODIFIED_DATE
FROM (
         SELECT
             USER_LABEL AS userLabel,
             CASE
                 WHEN RATE = 309 THEN
                     CASE
                         WHEN (NATIVE_EMS_NAME LIKE '%10GE-MoE%' OR NATIVE_EMS_NAME LIKE '%10GE-ETY%' OR NATIVE_EMS_NAME LIKE '%10GigE%') THEN '10GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%10G%' THEN '10GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%100GE-ETY%' OR NATIVE_EMS_NAME LIKE '%100GE-MoE%' OR NATIVE_EMS_NAME LIKE '%100GigE%') THEN '100GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%100G%' THEN '100GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%GE-MoE%' OR NATIVE_EMS_NAME LIKE '%1GigE%' OR NATIVE_EMS_NAME LIKE '%GE-ETY%') THEN '1GigE'
                         ELSE 'DWDM'
                         END
                 WHEN RATE = 19 THEN 'STM0'
                 WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                 WHEN RATE IN (74, 21, 26) THEN 'STM4'
                 WHEN RATE IN (75, 89, 88) THEN 'STM8'
                 WHEN RATE IN (76, 22, 27) THEN 'STM16'
                 WHEN RATE IN (77, 28, 23) THEN 'STM64'
                 WHEN RATE IN (78, 91, 90) THEN 'STM256'
                 WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113) THEN 'DWDM'
                 ELSE TO_CHAR(RATE)
                 END AS ethRate,
             TECHNOLOGY_LAYER AS technology,
             A_END_ME_LABEL AS aEndNode,
             Z_END_ME_LABEL AS zEndNode,
             A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
             Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS zEndPort,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS aEndVendor,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS zEndVendor,
             'ECI' AS VENDOR,
             CIRCLE AS circle,
             CASE
                 WHEN TECHNOLOGY_LAYER = 'ETH' THEN 'INNI Connectivity'
                 ELSE 'SDH_TOPOLOGICAL_LINK_CON'
                 END AS specification,
             A_END_EMS AS nmsName,
             NATIVE_EMS_NAME,
             RING_NAME,
             TOPOLOGY_TYPE,
             CAST(NULL AS TIMESTAMP) CREATED_BY,
             CAST(NULL AS TIMESTAMP) CREATED_ON,
             LAST_MODIFIED_DATE
         FROM ECI_STITCHING.ECI_TOPOLOGY_AP WHERE IS_DELETED = 0

         UNION ALL

         SELECT
             USER_LABEL AS userLabel,
             CASE
                 WHEN RATE = 309 THEN
                     CASE
                         WHEN (NATIVE_EMS_NAME LIKE '%10GE-MoE%' OR NATIVE_EMS_NAME LIKE '%10GE-ETY%' OR NATIVE_EMS_NAME LIKE '%10GigE%') THEN '10GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%10G%' THEN '10GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%100GE-ETY%' OR NATIVE_EMS_NAME LIKE '%100GE-MoE%' OR NATIVE_EMS_NAME LIKE '%100GigE%') THEN '100GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%100G%' THEN '100GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%GE-MoE%' OR NATIVE_EMS_NAME LIKE '%1GigE%' OR NATIVE_EMS_NAME LIKE '%GE-ETY%') THEN '1GigE'
                         ELSE 'DWDM'
                         END
                 WHEN RATE = 19 THEN 'STM0'
                 WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                 WHEN RATE IN (74, 21, 26) THEN 'STM4'
                 WHEN RATE IN (75, 89, 88) THEN 'STM8'
                 WHEN RATE IN (76, 22, 27) THEN 'STM16'
                 WHEN RATE IN (77, 28, 23) THEN 'STM64'
                 WHEN RATE IN (78, 91, 90) THEN 'STM256'
                 WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113) THEN 'DWDM'
                 ELSE TO_CHAR(RATE)
                 END AS ethRate,
             TECHNOLOGY_LAYER AS technology,
             A_END_ME_LABEL AS aEndNode,
             Z_END_ME_LABEL AS zEndNode,
             A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
             Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS zEndPort,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS aEndVendor,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS zEndVendor,
             'ECI' AS VENDOR,
             CIRCLE AS circle,
             CASE
                 WHEN TECHNOLOGY_LAYER = 'ETH' THEN 'INNI Connectivity'
                 ELSE 'SDH_TOPOLOGICAL_LINK_CON'
                 END AS specification,
             A_END_EMS AS nmsName,
             NATIVE_EMS_NAME,
             RING_NAME,
             TOPOLOGY_TYPE,
             CAST(NULL AS TIMESTAMP) CREATED_BY,
             CAST(NULL AS TIMESTAMP) CREATED_ON,
             LAST_MODIFIED_DATE
         FROM ECI_STITCHING.ECI_TOPOLOGY_APOLLO WHERE IS_DELETED = 0

         UNION ALL

         SELECT
             USER_LABEL AS userLabel,
             CASE
                 WHEN RATE = 309 THEN
                     CASE
                         WHEN (NATIVE_EMS_NAME LIKE '%10GE-MoE%' OR NATIVE_EMS_NAME LIKE '%10GE-ETY%' OR NATIVE_EMS_NAME LIKE '%10GigE%') THEN '10GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%10G%' THEN '10GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%100GE-ETY%' OR NATIVE_EMS_NAME LIKE '%100GE-MoE%' OR NATIVE_EMS_NAME LIKE '%100GigE%') THEN '100GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%100G%' THEN '100GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%GE-MoE%' OR NATIVE_EMS_NAME LIKE '%1GigE%' OR NATIVE_EMS_NAME LIKE '%GE-ETY%') THEN '1GigE'
                         ELSE 'DWDM'
                         END
                 WHEN RATE = 19 THEN 'STM0'
                 WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                 WHEN RATE IN (74, 21, 26) THEN 'STM4'
                 WHEN RATE IN (75, 89, 88) THEN 'STM8'
                 WHEN RATE IN (76, 22, 27) THEN 'STM16'
                 WHEN RATE IN (77, 28, 23) THEN 'STM64'
                 WHEN RATE IN (78, 91, 90) THEN 'STM256'
                 WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113) THEN 'DWDM'
                 ELSE TO_CHAR(RATE)
                 END AS ethRate,
             TECHNOLOGY_LAYER AS technology,
             A_END_ME_LABEL AS aEndNode,
             Z_END_ME_LABEL AS zEndNode,
             A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
             Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS zEndPort,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS aEndVendor,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS zEndVendor,
             'ECI' AS VENDOR,
             CIRCLE AS circle,
             CASE
                 WHEN TECHNOLOGY_LAYER = 'ETH' THEN 'INNI Connectivity'
                 ELSE 'SDH_TOPOLOGICAL_LINK_CON'
                 END AS specification,
             A_END_EMS AS nmsName,
             NATIVE_EMS_NAME,
             RING_NAME,
             TOPOLOGY_TYPE,
             CAST(NULL AS TIMESTAMP) CREATED_BY,
             CAST(NULL AS TIMESTAMP) CREATED_ON,
             LAST_MODIFIED_DATE FROM ECI_STITCHING.ECI_TOPOLOGY_DEL WHERE IS_DELETED = 0

         UNION ALL

         SELECT
             USER_LABEL AS userLabel,
             CASE
                 WHEN RATE = 309 THEN
                     CASE
                         WHEN (NATIVE_EMS_NAME LIKE '%10GE-MoE%' OR NATIVE_EMS_NAME LIKE '%10GE-ETY%' OR NATIVE_EMS_NAME LIKE '%10GigE%') THEN '10GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%10G%' THEN '10GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%100GE-ETY%' OR NATIVE_EMS_NAME LIKE '%100GE-MoE%' OR NATIVE_EMS_NAME LIKE '%100GigE%') THEN '100GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%100G%' THEN '100GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%GE-MoE%' OR NATIVE_EMS_NAME LIKE '%1GigE%' OR NATIVE_EMS_NAME LIKE '%GE-ETY%') THEN '1GigE'
                         ELSE 'DWDM'
                         END
                 WHEN RATE = 19 THEN 'STM0'
                 WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                 WHEN RATE IN (74, 21, 26) THEN 'STM4'
                 WHEN RATE IN (75, 89, 88) THEN 'STM8'
                 WHEN RATE IN (76, 22, 27) THEN 'STM16'
                 WHEN RATE IN (77, 28, 23) THEN 'STM64'
                 WHEN RATE IN (78, 91, 90) THEN 'STM256'
                 WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113) THEN 'DWDM'
                 ELSE TO_CHAR(RATE)
                 END AS ethRate,
             TECHNOLOGY_LAYER AS technology,
             A_END_ME_LABEL AS aEndNode,
             Z_END_ME_LABEL AS zEndNode,
             A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
             Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS zEndPort,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS aEndVendor,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS zEndVendor,
             'ECI' AS VENDOR,
             CIRCLE AS circle,
             CASE
                 WHEN TECHNOLOGY_LAYER = 'ETH' THEN 'INNI Connectivity'
                 ELSE 'SDH_TOPOLOGICAL_LINK_CON'
                 END AS specification,
             A_END_EMS AS nmsName,
             NATIVE_EMS_NAME,
             RING_NAME,
             TOPOLOGY_TYPE,
             CAST(NULL AS TIMESTAMP) CREATED_BY,
             CAST(NULL AS TIMESTAMP) CREATED_ON,
             LAST_MODIFIED_DATE
         FROM ECI_STITCHING.ECI_TOPOLOGY_GJ WHERE IS_DELETED = 0

         UNION ALL

         SELECT
             USER_LABEL AS userLabel,
             CASE
                 WHEN RATE = 309 THEN
                     CASE
                         WHEN (NATIVE_EMS_NAME LIKE '%10GE-MoE%' OR NATIVE_EMS_NAME LIKE '%10GE-ETY%' OR NATIVE_EMS_NAME LIKE '%10GigE%') THEN '10GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%10G%' THEN '10GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%100GE-ETY%' OR NATIVE_EMS_NAME LIKE '%100GE-MoE%' OR NATIVE_EMS_NAME LIKE '%100GigE%') THEN '100GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%100G%' THEN '100GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%GE-MoE%' OR NATIVE_EMS_NAME LIKE '%1GigE%' OR NATIVE_EMS_NAME LIKE '%GE-ETY%') THEN '1GigE'
                         ELSE 'DWDM'
                         END
                 WHEN RATE = 19 THEN 'STM0'
                 WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                 WHEN RATE IN (74, 21, 26) THEN 'STM4'
                 WHEN RATE IN (75, 89, 88) THEN 'STM8'
                 WHEN RATE IN (76, 22, 27) THEN 'STM16'
                 WHEN RATE IN (77, 28, 23) THEN 'STM64'
                 WHEN RATE IN (78, 91, 90) THEN 'STM256'
                 WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113) THEN 'DWDM'
                 ELSE TO_CHAR(RATE)
                 END AS ethRate,
             TECHNOLOGY_LAYER AS technology,
             A_END_ME_LABEL AS aEndNode,
             Z_END_ME_LABEL AS zEndNode,
             A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
             Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS zEndPort,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS aEndVendor,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS zEndVendor,
             'ECI' AS VENDOR,
             CIRCLE AS circle,
             CASE
                 WHEN TECHNOLOGY_LAYER = 'ETH' THEN 'INNI Connectivity'
                 ELSE 'SDH_TOPOLOGICAL_LINK_CON'
                 END AS specification,
             A_END_EMS AS nmsName,
             NATIVE_EMS_NAME,
             RING_NAME,
             TOPOLOGY_TYPE,
             CAST(NULL AS TIMESTAMP) CREATED_BY,
             CAST(NULL AS TIMESTAMP) CREATED_ON,
             LAST_MODIFIED_DATE
         FROM ECI_STITCHING.ECI_TOPOLOGY_KA WHERE IS_DELETED = 0

         UNION ALL

         SELECT
             USER_LABEL AS userLabel,
             CASE
                 WHEN RATE = 309 THEN
                     CASE
                         WHEN (NATIVE_EMS_NAME LIKE '%10GE-MoE%' OR NATIVE_EMS_NAME LIKE '%10GE-ETY%' OR NATIVE_EMS_NAME LIKE '%10GigE%') THEN '10GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%10G%' THEN '10GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%100GE-ETY%' OR NATIVE_EMS_NAME LIKE '%100GE-MoE%' OR NATIVE_EMS_NAME LIKE '%100GigE%') THEN '100GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%100G%' THEN '100GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%GE-MoE%' OR NATIVE_EMS_NAME LIKE '%1GigE%' OR NATIVE_EMS_NAME LIKE '%GE-ETY%') THEN '1GigE'
                         ELSE 'DWDM'
                         END
                 WHEN RATE = 19 THEN 'STM0'
                 WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                 WHEN RATE IN (74, 21, 26) THEN 'STM4'
                 WHEN RATE IN (75, 89, 88) THEN 'STM8'
                 WHEN RATE IN (76, 22, 27) THEN 'STM16'
                 WHEN RATE IN (77, 28, 23) THEN 'STM64'
                 WHEN RATE IN (78, 91, 90) THEN 'STM256'
                 WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113) THEN 'DWDM'
                 ELSE TO_CHAR(RATE)
                 END AS ethRate,
             TECHNOLOGY_LAYER AS technology,
             A_END_ME_LABEL AS aEndNode,
             Z_END_ME_LABEL AS zEndNode,
             A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
             Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS zEndPort,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS aEndVendor,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS zEndVendor,
             'ECI' AS VENDOR,
             CIRCLE AS circle,
             CASE
                 WHEN TECHNOLOGY_LAYER = 'ETH' THEN 'INNI Connectivity'
                 ELSE 'SDH_TOPOLOGICAL_LINK_CON'
                 END AS specification,
             A_END_EMS AS nmsName,
             NATIVE_EMS_NAME,
             RING_NAME,
             TOPOLOGY_TYPE,
             CAST(NULL AS TIMESTAMP) CREATED_BY,
             CAST(NULL AS TIMESTAMP) CREATED_ON,
             LAST_MODIFIED_DATE FROM ECI_STITCHING.ECI_TOPOLOGY_MUM WHERE IS_DELETED = 0

         UNION ALL

         SELECT
             USER_LABEL AS userLabel,
             CASE
                 WHEN RATE = 309 THEN
                     CASE
                         WHEN (NATIVE_EMS_NAME LIKE '%10GE-MoE%' OR NATIVE_EMS_NAME LIKE '%10GE-ETY%' OR NATIVE_EMS_NAME LIKE '%10GigE%') THEN '10GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%10G%' THEN '10GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%100GE-ETY%' OR NATIVE_EMS_NAME LIKE '%100GE-MoE%' OR NATIVE_EMS_NAME LIKE '%100GigE%') THEN '100GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%100G%' THEN '100GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%GE-MoE%' OR NATIVE_EMS_NAME LIKE '%1GigE%' OR NATIVE_EMS_NAME LIKE '%GE-ETY%') THEN '1GigE'
                         ELSE 'DWDM'
                         END
                 WHEN RATE = 19 THEN 'STM0'
                 WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                 WHEN RATE IN (74, 21, 26) THEN 'STM4'
                 WHEN RATE IN (75, 89, 88) THEN 'STM8'
                 WHEN RATE IN (76, 22, 27) THEN 'STM16'
                 WHEN RATE IN (77, 28, 23) THEN 'STM64'
                 WHEN RATE IN (78, 91, 90) THEN 'STM256'
                 WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113) THEN 'DWDM'
                 ELSE TO_CHAR(RATE)
                 END AS ethRate,
             TECHNOLOGY_LAYER AS technology,
             A_END_ME_LABEL AS aEndNode,
             Z_END_ME_LABEL AS zEndNode,
             A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
             Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS zEndPort,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS aEndVendor,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS zEndVendor,
             'ECI' AS VENDOR,
             CIRCLE AS circle,
             CASE
                 WHEN TECHNOLOGY_LAYER = 'ETH' THEN 'INNI Connectivity'
                 ELSE 'SDH_TOPOLOGICAL_LINK_CON'
                 END AS specification,
             A_END_EMS AS nmsName,
             NATIVE_EMS_NAME,
             RING_NAME,
             TOPOLOGY_TYPE,
             CAST(NULL AS TIMESTAMP) CREATED_BY,
             CAST(NULL AS TIMESTAMP) CREATED_ON,
             LAST_MODIFIED_DATE FROM ECI_STITCHING.ECI_TOPOLOGY_ROM WHERE IS_DELETED = 0

         UNION ALL

         SELECT
             USER_LABEL AS userLabel,
             CASE
                 WHEN RATE = 309 THEN
                     CASE
                         WHEN (NATIVE_EMS_NAME LIKE '%10GE-MoE%' OR NATIVE_EMS_NAME LIKE '%10GE-ETY%' OR NATIVE_EMS_NAME LIKE '%10GigE%') THEN '10GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%10G%' THEN '10GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%100GE-ETY%' OR NATIVE_EMS_NAME LIKE '%100GE-MoE%' OR NATIVE_EMS_NAME LIKE '%100GigE%') THEN '100GigE'
                         WHEN NATIVE_EMS_NAME LIKE '%100G%' THEN '100GigE'
                         WHEN (NATIVE_EMS_NAME LIKE '%GE-MoE%' OR NATIVE_EMS_NAME LIKE '%1GigE%' OR NATIVE_EMS_NAME LIKE '%GE-ETY%') THEN '1GigE'
                         ELSE 'DWDM'
                         END
                 WHEN RATE = 19 THEN 'STM0'
                 WHEN RATE IN (73, 25, 20, 93) THEN 'STM1'
                 WHEN RATE IN (74, 21, 26) THEN 'STM4'
                 WHEN RATE IN (75, 89, 88) THEN 'STM8'
                 WHEN RATE IN (76, 22, 27) THEN 'STM16'
                 WHEN RATE IN (77, 28, 23) THEN 'STM64'
                 WHEN RATE IN (78, 91, 90) THEN 'STM256'
                 WHEN RATE IN (1, 40, 41, 42, 47, 49, 105, 111, 113) THEN 'DWDM'
                 ELSE TO_CHAR(RATE)
                 END AS ethRate,
             TECHNOLOGY_LAYER AS technology,
             A_END_ME_LABEL AS aEndNode,
             Z_END_ME_LABEL AS zEndNode,
             A_END_PORT_LABEL || '#' || A_END_PORT_NAME AS aEndPort,
             Z_END_PORT_LABEL || '#' || Z_END_PORT_NAME AS zEndPort,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS aEndVendor,
             CASE
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 WHEN A_END_PORT_NAME NOT LIKE '%UME%' AND Z_END_PORT_NAME NOT LIKE '%UME%' THEN 'ECI'
                 WHEN A_END_PORT_NAME LIKE '%UME%' AND Z_END_PORT_NAME LIKE '%UME%' THEN NULL
                 END AS zEndVendor,
             'ECI' AS VENDOR,
             CIRCLE AS circle,
             CASE
                 WHEN TECHNOLOGY_LAYER = 'ETH' THEN 'INNI Connectivity'
                 ELSE 'SDH_TOPOLOGICAL_LINK_CON'
                 END AS specification,
             A_END_EMS AS nmsName,
             NATIVE_EMS_NAME,
             RING_NAME,
             TOPOLOGY_TYPE,
             CAST(NULL AS TIMESTAMP) CREATED_BY,
             CAST(NULL AS TIMESTAMP) CREATED_ON,
             LAST_MODIFIED_DATE FROM ECI_STITCHING.ECI_TOPOLOGY_TN WHERE IS_DELETED = 0
     )