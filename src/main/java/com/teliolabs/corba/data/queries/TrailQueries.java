package com.teliolabs.corba.data.queries;

public class TrailQueries {
    public static final String INSERT_SDH_TRAIL = "INSERT INTO \"%s\" (" +
            "\"TRAIL_ID\", \"USER_LABEL\", \"CIRCUIT_ID\", \"RATE\", \"TECHNOLOGY\", \"SPECIFICATION\", " +
            "\"PATH_TYPE\", \"TOPOLOGY\", \"A_END_DROP_NODE\", \"Z_END_DROP_NODE\", " +
            "\"A_END_DROP_PORT\", \"Z_END_DROP_PORT\", \"SEQUENCE\", \"CHANNEL\", \"A_END_NODE\", " +
            "\"Z_END_NODE\", \"A_END_PORT\", \"Z_END_PORT\", \"TOPOLOGY_TYPE\", " +
            "\"SRF_ID\", \"LAST_MODIFIED_TIME\"" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


    public static final String INSERT_EOS_TRAIL = "INSERT INTO \"%s\""
            + " (TRAIL_ID, USER_LABEL, V_CAT, CIRCUIT_ID, SRF_ID, LAYER_RATE, RATE, TECHNOLOGY , SPECIFICATION, PATH_TYPE, "
            + " TOPOLOGY, A_END_DROP_NODE, Z_END_DROP_NODE, A_END_DROP_PORT, Z_END_DROP_PORT, SEQUENCE, CHANNEL, A_END_NODE, Z_END_NODE, A_END_PORT, "
            + " Z_END_PORT, TOPOLOGY_TYPE, LAST_MODIFIED_TIME) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public static final String SELECT_SNC = "SELECT a.snc_id                 AS trail_id,\n" +
            "       CASE\n" +
            "           WHEN a.SNC_NAME NOT LIKE '%EXT%' THEN\n" +
            "               a.SNC_NAME\n" +
            "                   || '#'\n" +
            "                   || a.snc_id\n" +
            "           ELSE\n" +
            "               a.SNC_NAME\n" +
            "           END                  AS user_label,\n" +
            "       sn.circuit_id            AS circuit_id,\n" +
            "       sn.srf_id,\n" +
            "       CASE\n" +
            "           WHEN sn.SNC_RATE = 'VC12' THEN\n" +
            "               'E1'\n" +
            "           ELSE\n" +
            "               sn.SNC_RATE\n" +
            "           END                  AS rate,\n" +
            "       CASE\n" +
            "           WHEN sn.SNC_RATE = 'VC12' THEN\n" +
            "               'E-Carrier'\n" +
            "           ELSE\n" +
            "               t.TECHNOLOGY_LAYER\n" +
            "           END                  AS technology,\n" +
            "       'SDH_TRAILS_CON'         AS specification,\n" +
            "       a.path_type              AS path_type,\n" +
            "       'ECI'                    AS vendor,\n" +
            "       t.user_label             AS topology,\n" +
            "       CASE\n" +
            "           WHEN sn.A_END_ME NOT LIKE '%UME%'\n" +
            "               THEN\n" +
            "               sn.A_END_ME_LABEL\n" +
            "           WHEN sn.A_END_ME LIKE '%UME%'\n" +
            "               THEN\n" +
            "               (SELECT NVL(p.ME_LABEL, 'N/F')\n" +
            "                FROM \"%s\" r\n" +
            "                         LEFT JOIN \"%s\" p\n" +
            "                                   ON r.Z_END_ME = p.ME_NAME\n" +
            "                                       AND r.Z_END_PTP = p.ptp_id\n" +
            "                WHERE r.snc_id = a.snc_id\n" +
            "                  AND r.path_type = 'MAIN'\n" +
            "                  AND r.TUPLE_A = '1'\n" +
            "                  AND r.A_END_ME NOT LIKE '%UME%'\n" +
            "                  AND r.Z_END_ME NOT LIKE '%UME%'\n" +
            "                  AND r.TUPLE_B =\n" +
            "                      (SELECT (MAX(TO_NUMBER(TUPLE_B)))\n" +
            "                       FROM \"%s\"\n" +
            "                       WHERE snc_id = a.snc_id\n" +
            "                         AND path_type = 'MAIN'\n" +
            "                         AND TUPLE_A = '1'\n" +
            "                         AND A_END_ME NOT LIKE '%UME%'\n" +
            "                         AND Z_END_ME NOT LIKE '%UME%')\n" +
            "                  AND ROWNUM = 1)\n" +
            "           ELSE\n" +
            "               NULL\n" +
            "           END                  AS a_end_drop_node,\n" +
            "       CASE\n" +
            "           WHEN sn.Z_END_ME NOT LIKE '%UME%'\n" +
            "               THEN\n" +
            "               sn.Z_END_ME_LABEL\n" +
            "           WHEN sn.Z_END_ME LIKE '%UME%'\n" +
            "               THEN\n" +
            "               (SELECT NVL(p.ME_LABEL, 'N/F')\n" +
            "                FROM % r\n" +
            "                         LEFT JOIN % p\n" +
            "                                   ON r.Z_END_ME = p.ME_NAME\n" +
            "                                       AND r.Z_END_PTP = p.ptp_id\n" +
            "                WHERE r.snc_id = a.snc_id\n" +
            "                  AND r.path_type = 'MAIN'\n" +
            "                  AND r.TUPLE_A = '1'\n" +
            "                  AND r.A_END_ME NOT LIKE '%UME%'\n" +
            "                  AND r.Z_END_ME NOT LIKE '%UME%'\n" +
            "                  AND r.TUPLE_B =\n" +
            "                      (SELECT (MIN(TO_NUMBER(TUPLE_B)))\n" +
            "                       FROM %\n" +
            "                       WHERE snc_id = a.snc_id\n" +
            "                         AND path_type = 'MAIN'\n" +
            "                         AND TUPLE_A = '1'\n" +
            "                         AND A_END_ME NOT LIKE '%UME%'\n" +
            "                         AND Z_END_ME NOT LIKE '%UME%')\n" +
            "                  AND ROWNUM = 1)\n" +
            "           ELSE\n" +
            "               NULL\n" +
            "           END                  AS z_end_drop_node,\n" +
            "       CASE\n" +
            "           WHEN sn.A_END_ME NOT LIKE '%UME%'\n" +
            "               THEN\n" +
            "               sn.A_END_ME_LABEL || '#' || sn.A_END_PTP\n" +
            "           WHEN sn.A_END_ME LIKE '%UME%'\n" +
            "               THEN\n" +
            "               (SELECT p.port_native_name || '#' || p.ptp_id\n" +
            "                FROM % r\n" +
            "                         LEFT JOIN % p\n" +
            "                                   ON r.Z_END_ME = p.ME_NAME\n" +
            "                                       AND r.Z_END_PTP = p.ptp_id\n" +
            "                WHERE r.snc_id = a.snc_id\n" +
            "                  AND r.path_type = 'MAIN'\n" +
            "                  AND r.TUPLE_A = '1'\n" +
            "                  AND r.A_END_ME NOT LIKE '%UME%'\n" +
            "                  AND r.Z_END_ME NOT LIKE '%UME%'\n" +
            "                  AND r.TUPLE_B =\n" +
            "                      (SELECT (MAX(TO_NUMBER(TUPLE_B)))\n" +
            "                       FROM %\n" +
            "                       WHERE snc_id = a.snc_id\n" +
            "                         AND path_type = 'MAIN'\n" +
            "                         AND TUPLE_A = '1'\n" +
            "                         AND A_END_ME NOT LIKE '%UME%'\n" +
            "                         AND Z_END_ME NOT LIKE '%UME%')\n" +
            "                  AND ROWNUM = 1)\n" +
            "           ELSE\n" +
            "               NULL\n" +
            "           END                  AS a_end_drop_port,\n" +
            "       CASE\n" +
            "           WHEN sn.Z_END_ME NOT LIKE '%UME%'\n" +
            "               THEN\n" +
            "               sn.Z_END_ME_LABEL || '#' || sn.Z_END_PTP\n" +
            "           WHEN sn.Z_END_ME LIKE '%UME%'\n" +
            "               THEN\n" +
            "               (SELECT p.port_native_name || '#' || p.ptp_id\n" +
            "                FROM % r\n" +
            "                         LEFT JOIN % p\n" +
            "                                   ON r.Z_END_ME = p.ME_NAME\n" +
            "                                       AND r.Z_END_PTP = p.ptp_id\n" +
            "                WHERE r.snc_id = a.snc_id\n" +
            "                  AND r.path_type = 'MAIN'\n" +
            "                  AND r.TUPLE_A = '1'\n" +
            "                  AND r.A_END_ME NOT LIKE '%UME%'\n" +
            "                  AND r.Z_END_ME NOT LIKE '%UME%'\n" +
            "                  AND r.TUPLE_B =\n" +
            "                      (SELECT (MIN(TO_NUMBER(TUPLE_B)))\n" +
            "                       FROM %\n" +
            "                       WHERE snc_id = a.snc_id\n" +
            "                         AND path_type = 'MAIN'\n" +
            "                         AND TUPLE_A = '1'\n" +
            "                         AND A_END_ME NOT LIKE '%UME%'\n" +
            "                         AND Z_END_ME NOT LIKE '%UME%')\n" +
            "                  AND ROWNUM = 1)\n" +
            "           ELSE\n" +
            "               NULL\n" +
            "           END                  AS z_end_drop_port,\n" +
            "       ROWNUM                   AS sequence,\n" +
            "       a.A_END_CTP              AS channel,\n" +
            "       t.A_END_ME_LABEL         AS a_end_node,\n" +
            "       t.Z_END_ME_LABEL         AS z_end_node,\n" +
            "       t.A_END_PORT_LABEL\n" +
            "           || '#'\n" +
            "           || t.A_END_PORT_NAME AS a_end_port,\n" +
            "       t.Z_END_PORT_LABEL\n" +
            "           || '#'\n" +
            "           || t.Z_END_PORT_NAME AS z_end_port,\n" +
            "       CASE\n" +
            "           WHEN (t.A_END_ME_NAME NOT LIKE '%UME%'\n" +
            "               AND t.Z_END_ME_NAME LIKE '%UME%')\n" +
            "               OR (t.A_END_ME_NAME LIKE '%UME%'\n" +
            "                   AND t.Z_END_ME_NAME NOT LIKE '%UME%') THEN\n" +
            "               'NE2VNE'\n" +
            "           WHEN t.A_END_ME_NAME NOT LIKE '%UME%'\n" +
            "               AND t.Z_END_ME_NAME NOT LIKE '%UME%' THEN\n" +
            "               'NE2NE'\n" +
            "           WHEN t.A_END_ME_NAME LIKE '%UME%'\n" +
            "               AND t.Z_END_ME_NAME LIKE '%UME%' THEN\n" +
            "               'VNE2VNE'\n" +
            "           ELSE\n" +
            "               NULL\n" +
            "           END                  AS topology_type,\n" +
            "       t.circle\n" +
            "FROM (SELECT *\n" +
            "      FROM (SELECT r1.snc_id,\n" +
            "                   r2.tuple_b,\n" +
            "                   r1.snc_name,\n" +
            "                   r2.a_end_me,\n" +
            "                   p1.me_label,\n" +
            "                   r2.a_end_ptp,\n" +
            "                   p1.port_native_name AS aportlabel,\n" +
            "                   r2.a_end_ctp,\n" +
            "                   r1.z_end_me,\n" +
            "                   p2.me_label         AS zlabel,\n" +
            "                   r1.z_end_ptp,\n" +
            "                   p2.port_native_name AS zportlabel,\n" +
            "                   r1.z_end_ctp,\n" +
            "                   r1.tuple_b             bval,\n" +
            "                   r1.tuple_a             aval,\n" +
            "                   r1.path_type\n" +
            "            FROM % r1,\n" +
            "                 % r2,\n" +
            "                 % p1,\n" +
            "                 % p2\n" +
            "            WHERE r1.snc_id = r2.snc_id\n" +
            "              AND r1.tuple_a = '1'\n" +
            "              AND r2.tuple_a = '1'\n" +
            "              AND r1.path_type = 'MAIN'\n" +
            "              AND r2.path_type = 'MAIN'\n" +
            "              AND (TO_NUMBER(r2.tuple_b) - TO_NUMBER(r1.tuple_b) = 2)\n" +
            "              AND r1.snc_id = ?\n" +
            "              AND r2.a_end_me = p1.ME_NAME\n" +
            "              AND r2.a_end_ptp = p1.ptp_id\n" +
            "              AND r1.z_end_me = p2.ME_NAME\n" +
            "              AND r1.z_end_ptp = p2.ptp_id\n" +
            "            UNION\n" +
            "            SELECT r1.snc_id,\n" +
            "                   r2.tuple_b,\n" +
            "                   r1.snc_name,\n" +
            "                   r2.a_end_me,\n" +
            "                   p1.me_label,\n" +
            "                   r2.a_end_ptp,\n" +
            "                   p1.port_native_name AS aportlabel,\n" +
            "                   r2.a_end_ctp,\n" +
            "                   r1.z_end_me,\n" +
            "                   p2.me_label         AS zlabel,\n" +
            "                   r1.z_end_ptp,\n" +
            "                   p2.port_native_name AS zportlabel,\n" +
            "                   r1.z_end_ctp,\n" +
            "                   r1.tuple_b             bval,\n" +
            "                   r1.tuple_a             aval,\n" +
            "                   r1.path_type\n" +
            "            FROM % r1,\n" +
            "                 % r2,\n" +
            "                 % p1,\n" +
            "                 % p2\n" +
            "            WHERE r1.snc_id = r2.snc_id\n" +
            "              AND r1.tuple_a = '2'\n" +
            "              AND r2.tuple_a = '2'\n" +
            "              AND r1.path_type = 'PROTECTION'\n" +
            "              AND r2.path_type = 'PROTECTION'\n" +
            "              AND (TO_NUMBER(r2.tuple_b) - TO_NUMBER(r1.tuple_b) = 2)\n" +
            "              AND r1.snc_id = ?\n" +
            "              AND r2.a_end_me = p1.ME_NAME\n" +
            "              AND r2.a_end_ptp = p1.ptp_id\n" +
            "              AND r1.z_end_me = p2.ME_NAME\n" +
            "              AND r1.z_end_ptp = p2.ptp_id) k\n" +
            "      ORDER BY k.path_type,\n" +
            "               TO_NUMBER(k.bval) DESC) a,\n" +
            "     % t,\n" +
            "     % sn\n" +
            "WHERE ((t.A_END_ME_NAME = a.A_END_ME\n" +
            "    AND t.A_END_PORT_NAME = a.A_END_PTP\n" +
            "    AND t.Z_END_ME_NAME = a.Z_END_ME\n" +
            "    AND t.Z_END_PORT_NAME = a.Z_END_PTP)\n" +
            "    OR (t.A_END_ME_NAME = a.Z_END_ME\n" +
            "        AND t.A_END_PORT_NAME = a.Z_END_PTP\n" +
            "        AND t.Z_END_ME_NAME = a.A_END_ME\n" +
            "        AND t.Z_END_PORT_NAME = a.A_END_PTP))\n" +
            "  AND t.TECHNOLOGY_LAYER = 'SDH'\n" +
            "  AND a.snc_id = sn.SNC_ID";

}
