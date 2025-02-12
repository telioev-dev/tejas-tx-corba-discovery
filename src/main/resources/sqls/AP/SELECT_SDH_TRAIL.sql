SELECT a.snc_id                 AS trail_id,
       CASE
           WHEN a.SNC_NAME NOT LIKE '%EXT%' THEN
               a.SNC_NAME
                   || '#'
                   || a.snc_id
           ELSE
               a.SNC_NAME
           END                  AS user_label,
       sn.circuit_id            AS circuit_id,
       sn.srf_id,
       CASE
           WHEN sn.SNC_RATE = 'VC12' THEN
               'E1'
           ELSE
               sn.SNC_RATE
           END                  AS rate,
       CASE
           WHEN sn.SNC_RATE = 'VC12' THEN
               'E-Carrier'
           ELSE
               t.TECHNOLOGY_LAYER
           END                  AS technology,
       'SDH_TRAILS_CON'         AS specification,
       a.path_type              AS path_type,
       'ECI'                    AS vendor,
       t.user_label             AS topology,
       CASE
           WHEN sn.A_END_ME NOT LIKE '%UME%'
               THEN
               sn.A_END_ME_LABEL
           WHEN sn.A_END_ME LIKE '%UME%'
               THEN
               (SELECT NVL(p.ME_LABEL, 'N/F')
                FROM eci_route_ap r
                         LEFT JOIN eci_ptp_ap p
                                   ON r.Z_END_ME = p.ME_NAME
                                       AND r.Z_END_PTP = p.ptp_id
                WHERE r.snc_id = a.snc_id
                  AND r.path_type = 'MAIN'
                  AND r.TUPLE_A = '1'
                  AND r.A_END_ME NOT LIKE '%UME%'
                  AND r.Z_END_ME NOT LIKE '%UME%'
                  AND r.TUPLE_B =
                      (SELECT (MAX(TO_NUMBER(TUPLE_B)))
                       FROM eci_route_ap
                       WHERE snc_id = a.snc_id
                         AND path_type = 'MAIN'
                         AND TUPLE_A = '1'
                         AND A_END_ME NOT LIKE '%UME%'
                         AND Z_END_ME NOT LIKE '%UME%')
                  AND ROWNUM = 1)
           ELSE
               NULL
           END                  AS a_end_drop_node,
       CASE
           WHEN sn.Z_END_ME NOT LIKE '%UME%'
               THEN
               sn.Z_END_ME_LABEL
           WHEN sn.Z_END_ME LIKE '%UME%'
               THEN
               (SELECT NVL(p.ME_LABEL, 'N/F')
                FROM eci_route_ap r
                         LEFT JOIN eci_ptp_ap p
                                   ON r.Z_END_ME = p.ME_NAME
                                       AND r.Z_END_PTP = p.ptp_id
                WHERE r.snc_id = a.snc_id
                  AND r.path_type = 'MAIN'
                  AND r.TUPLE_A = '1'
                  AND r.A_END_ME NOT LIKE '%UME%'
                  AND r.Z_END_ME NOT LIKE '%UME%'
                  AND r.TUPLE_B =
                      (SELECT (MIN(TO_NUMBER(TUPLE_B)))
                       FROM eci_route_ap
                       WHERE snc_id = a.snc_id
                         AND path_type = 'MAIN'
                         AND TUPLE_A = '1'
                         AND A_END_ME NOT LIKE '%UME%'
                         AND Z_END_ME NOT LIKE '%UME%')
                  AND ROWNUM = 1)
           ELSE
               NULL
           END                  AS z_end_drop_node,
       CASE
           WHEN sn.A_END_ME NOT LIKE '%UME%'
               THEN
               sn.A_END_ME_LABEL || '#' || sn.A_END_PTP
           WHEN sn.A_END_ME LIKE '%UME%'
               THEN
               (SELECT p.port_native_name || '#' || p.ptp_id
                FROM eci_route_ap r
                         LEFT JOIN eci_ptp_ap p
                                   ON r.Z_END_ME = p.ME_NAME
                                       AND r.Z_END_PTP = p.ptp_id
                WHERE r.snc_id = a.snc_id
                  AND r.path_type = 'MAIN'
                  AND r.TUPLE_A = '1'
                  AND r.A_END_ME NOT LIKE '%UME%'
                  AND r.Z_END_ME NOT LIKE '%UME%'
                  AND r.TUPLE_B =
                      (SELECT (MAX(TO_NUMBER(TUPLE_B)))
                       FROM eci_route_ap
                       WHERE snc_id = a.snc_id
                         AND path_type = 'MAIN'
                         AND TUPLE_A = '1'
                         AND A_END_ME NOT LIKE '%UME%'
                         AND Z_END_ME NOT LIKE '%UME%')
                  AND ROWNUM = 1)
           ELSE
               NULL
           END                  AS a_end_drop_port,
       CASE
           WHEN sn.Z_END_ME NOT LIKE '%UME%'
               THEN
               sn.Z_END_ME_LABEL || '#' || sn.Z_END_PTP
           WHEN sn.Z_END_ME LIKE '%UME%'
               THEN
               (SELECT p.port_native_name || '#' || p.ptp_id
                FROM eci_route_ap r
                         LEFT JOIN eci_ptp_ap p
                                   ON r.Z_END_ME = p.ME_NAME
                                       AND r.Z_END_PTP = p.ptp_id
                WHERE r.snc_id = a.snc_id
                  AND r.path_type = 'MAIN'
                  AND r.TUPLE_A = '1'
                  AND r.A_END_ME NOT LIKE '%UME%'
                  AND r.Z_END_ME NOT LIKE '%UME%'
                  AND r.TUPLE_B =
                      (SELECT (MIN(TO_NUMBER(TUPLE_B)))
                       FROM eci_route_ap
                       WHERE snc_id = a.snc_id
                         AND path_type = 'MAIN'
                         AND TUPLE_A = '1'
                         AND A_END_ME NOT LIKE '%UME%'
                         AND Z_END_ME NOT LIKE '%UME%')
                  AND ROWNUM = 1)
           ELSE
               NULL
           END                  AS z_end_drop_port,
       ROWNUM                   AS sequence,
       a.A_END_CTP              AS channel,
       t.A_END_ME_LABEL         AS a_end_node,
       t.Z_END_ME_LABEL         AS z_end_node,
       t.A_END_PORT_LABEL
           || '#'
           || t.A_END_PORT_NAME AS a_end_port,
       t.Z_END_PORT_LABEL
           || '#'
           || t.Z_END_PORT_NAME AS z_end_port,
       CASE
           WHEN (t.A_END_ME_NAME NOT LIKE '%UME%'
               AND t.Z_END_ME_NAME LIKE '%UME%')
               OR (t.A_END_ME_NAME LIKE '%UME%'
                   AND t.Z_END_ME_NAME NOT LIKE '%UME%') THEN
               'NE2VNE'
           WHEN t.A_END_ME_NAME NOT LIKE '%UME%'
               AND t.Z_END_ME_NAME NOT LIKE '%UME%' THEN
               'NE2NE'
           WHEN t.A_END_ME_NAME LIKE '%UME%'
               AND t.Z_END_ME_NAME LIKE '%UME%' THEN
               'VNE2VNE'
           ELSE
               NULL
           END                  AS topology_type,
       t.circle
FROM (SELECT *
      FROM (SELECT r1.snc_id,
                   r2.tuple_b,
                   r1.snc_name,
                   r2.a_end_me,
                   p1.me_label,
                   r2.a_end_ptp,
                   p1.port_native_name AS aportlabel,
                   r2.a_end_ctp,
                   r1.z_end_me,
                   p2.me_label         AS zlabel,
                   r1.z_end_ptp,
                   p2.port_native_name AS zportlabel,
                   r1.z_end_ctp,
                   r1.tuple_b             bval,
                   r1.tuple_a             aval,
                   r1.path_type
            FROM eci_route_ap r1,
                 eci_route_ap r2,
                 eci_ptp_ap p1,
                 eci_ptp_ap p2
            WHERE r1.snc_id = r2.snc_id
              AND r1.tuple_a = '1'
              AND r2.tuple_a = '1'
              AND r1.path_type = 'MAIN'
              AND r2.path_type = 'MAIN'
              AND (TO_NUMBER(r2.tuple_b) - TO_NUMBER(r1.tuple_b) = 2)
              AND r1.snc_id = ?
              AND r2.a_end_me = p1.ME_NAME
              AND r2.a_end_ptp = p1.ptp_id
              AND r1.z_end_me = p2.ME_NAME
              AND r1.z_end_ptp = p2.ptp_id
            UNION
            SELECT r1.snc_id,
                   r2.tuple_b,
                   r1.snc_name,
                   r2.a_end_me,
                   p1.me_label,
                   r2.a_end_ptp,
                   p1.port_native_name AS aportlabel,
                   r2.a_end_ctp,
                   r1.z_end_me,
                   p2.me_label         AS zlabel,
                   r1.z_end_ptp,
                   p2.port_native_name AS zportlabel,
                   r1.z_end_ctp,
                   r1.tuple_b             bval,
                   r1.tuple_a             aval,
                   r1.path_type
            FROM eci_route_ap r1,
                 eci_route_ap r2,
                 eci_ptp_ap p1,
                 eci_ptp_ap p2
            WHERE r1.snc_id = r2.snc_id
              AND r1.tuple_a = '2'
              AND r2.tuple_a = '2'
              AND r1.path_type = 'PROTECTION'
              AND r2.path_type = 'PROTECTION'
              AND (TO_NUMBER(r2.tuple_b) - TO_NUMBER(r1.tuple_b) = 2)
              AND r1.snc_id = ?
              AND r2.a_end_me = p1.ME_NAME
              AND r2.a_end_ptp = p1.ptp_id
              AND r1.z_end_me = p2.ME_NAME
              AND r1.z_end_ptp = p2.ptp_id) k
      ORDER BY k.path_type,
               TO_NUMBER(k.bval) DESC) a,
     eci_topology_ap t,
     eci_snc_ap sn
WHERE ((t.A_END_ME_NAME = a.A_END_ME
    AND t.A_END_PORT_NAME = a.A_END_PTP
    AND t.Z_END_ME_NAME = a.Z_END_ME
    AND t.Z_END_PORT_NAME = a.Z_END_PTP)
    OR (t.A_END_ME_NAME = a.Z_END_ME
        AND t.A_END_PORT_NAME = a.Z_END_PTP
        AND t.Z_END_ME_NAME = a.A_END_ME
        AND t.Z_END_PORT_NAME = a.A_END_PTP))
  AND t.TECHNOLOGY_LAYER = 'SDH'
  AND a.snc_id = sn.SNC_ID