package com.teliolabs.corba.utils;

import com.teliolabs.corba.data.dto.Topology;
import lombok.extern.log4j.Log4j2;

import java.util.HashMap;
import java.util.Map;

@Log4j2
public class VendorUtils {

    private static Map<String, String> utStarNodeChassisMap = new HashMap<>();
    private static final String TOPOLOGY_REGEX = "([_-]?Z{4,}[_-]*)";
    private static final String ZTE_TOPOLOGY_REGEX = "\\bSL0([1-9])\\b";

    static {
        // Add entries to the map
        utStarNodeChassisMap.put("VSB-DL-CHS-021", "171");
        utStarNodeChassisMap.put("HYD-AP-CHS-011", "108");
        utStarNodeChassisMap.put("TUR-MU-CHS-001", "156");
        utStarNodeChassisMap.put("SDF-KO-CHS-062", "251");
        utStarNodeChassisMap.put("VSB-DL-CHS-069", "177");
        utStarNodeChassisMap.put("BLR-KA-CHS-013", "95");
        utStarNodeChassisMap.put("VSB-UE-CHS-025", "175");
        utStarNodeChassisMap.put("VSB-DL-CHS-023", "173");
        utStarNodeChassisMap.put("CHI-TN-CHS-017", "145");
        utStarNodeChassisMap.put("CON-KL-CHS-049", "147");
        utStarNodeChassisMap.put("HYD-AP-CHS-010", "107");
        utStarNodeChassisMap.put("TUR-MU-CHS-033", "168");
        utStarNodeChassisMap.put("VSB-DL-CHS-022", "172");
        utStarNodeChassisMap.put("BLR-KA-CHS-066", "100");
        utStarNodeChassisMap.put("HYD-AP-CHS-053", "111");
        utStarNodeChassisMap.put("VSB-UW-CHS-026", "176");
        utStarNodeChassisMap.put("TEC-MU-CHS-040", "139");
        utStarNodeChassisMap.put("TUR-MU-CHS-034", "169");
        utStarNodeChassisMap.put("BLR-KA-CHS-014", "96");
        utStarNodeChassisMap.put("PUN-MH-CHS-006", "194");
        utStarNodeChassisMap.put("TUR-MU-CHS-004", "167");
        utStarNodeChassisMap.put("AHM-GJ-CHS-046", "198");
        utStarNodeChassisMap.put("CHI-TN-CHS-018", "146");
        utStarNodeChassisMap.put("TUR-MU-CHS-003", "158");
        utStarNodeChassisMap.put("TUR-MU-CHS-002", "157");
        utStarNodeChassisMap.put("VSB-DL-CHS-024", "174");
        utStarNodeChassisMap.put("BLR-KA-CHS-065", "99");
        utStarNodeChassisMap.put("TEC-MU-CHS-038", "137");
        utStarNodeChassisMap.put("TEC-MU-CHS-042", "141");
        utStarNodeChassisMap.put("SDF-KO-CHS-061", "250");
        utStarNodeChassisMap.put("BLR-KA-CHS-015", "97");
        utStarNodeChassisMap.put("BLR-KA-CHS-016", "98");
        utStarNodeChassisMap.put("PUN-MH-CHS-005", "193");
        utStarNodeChassisMap.put("TEC-MU-CHS-039", "138");
        utStarNodeChassisMap.put("AHM-GJ-CHS-045", "197");
        utStarNodeChassisMap.put("AHM-GJ-CHS-047", "199");
        utStarNodeChassisMap.put("TEC-MU-CHS-037", "136");
        utStarNodeChassisMap.put("HYD-AP-CHS-009", "106");
        utStarNodeChassisMap.put("TEC-MU-CHS-041", "140");
        utStarNodeChassisMap.put("TEC-MU-CHS-043", "142");
        utStarNodeChassisMap.put("HYD-AP-CHS-012", "110");
        utStarNodeChassisMap.put("BPL-MP-CHS-057", "220");
    }

    public static void processTopology(Topology topology) {
        if (isZTE(topology)) {
            processZTE(topology);
        } else if (isUTStar(topology)) {
            processUTStar(topology);
        }
    }

    private static boolean isZTE(Topology topology) {
        return topology.getUserLabel().contains(ApplicationConstants.ZTE_IDENTIFIER_STR);
    }

    private static boolean isUTStar(Topology topology) {
        return topology.getUserLabel().contains(ApplicationConstants.UT_STAR_IDENTIFIER_STR);
    }

    private static void processZTE(Topology topology) {
        String userLabel = topology.getUserLabel();
        String[] parts = userLabel.split(TOPOLOGY_REGEX);
        if (parts.length == 3) {
            String nodePort = parts[2];
            String node = nodePort.substring(2).split("-RK")[0];
            String[] zteParts = nodePort.substring(2).split("-");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < zteParts.length - 1; i++) {
                result.append(zteParts[i]).append("-");
            }
            result.append(zteParts[zteParts.length - 1].replaceFirst("^[^P]*", ""));
            String portUserLabel = result.toString();
            portUserLabel = portUserLabel.replaceAll(ZTE_TOPOLOGY_REGEX, "SL$1");
            if (ManagedElementUtils.isUME(topology.getAEndMeName())) {
                topology.setAEndMeLabel(node);
                topology.setAEndPortLabel(portUserLabel);
            } else if (ManagedElementUtils.isUME(topology.getZEndMeName())) {
                topology.setZEndMeLabel(node);
                topology.setZEndPortLabel(portUserLabel);
            }
        }
    }

    private static void processUTStar(Topology topology) {
        String userLabel = topology.getUserLabel();
        String[] parts = userLabel.split(TOPOLOGY_REGEX);
        if (parts.length != 3) {
            return;
        }
        String nodePort = parts[2];
        String node = nodePort.split("_TMU")[0];
        String startPattern = "_TMU=";
        String endPattern = "_STM";

        // Find the index positions of the start and end patterns
        int startIndex = nodePort.indexOf(startPattern);
        int endIndex = nodePort.indexOf(endPattern);
        // Extract the substring between start and end patterns
        String slotNo = "";
        if (startIndex != -1 && endIndex != -1) {
            // Adjust startIndex to point to the end of startPattern
            startIndex += startPattern.length();
            // Extract substring
            slotNo = nodePort.substring(startIndex, endIndex);
        }

        StringBuilder zEndNodeSb = new StringBuilder(node);
        zEndNodeSb.append(" - ").append(utStarNodeChassisMap.getOrDefault(node, "")).append("/Slot-").append(Integer.parseInt(slotNo)).append("/BOARD-TMU-S");
        if (nodePort.contains("STM1No\\=2")) {
            zEndNodeSb.append("_PORTS_2");
        } else if (nodePort.contains("STM1No\\=1")) {
            zEndNodeSb.append("_PORTS_1");
        }

        if (ManagedElementUtils.isUME(topology.getAEndMeName())) {
            topology.setAEndMeLabel(node);
            topology.setAEndPortLabel(zEndNodeSb.toString());
        } else if (ManagedElementUtils.isUME(topology.getZEndMeName())) {
            topology.setZEndMeLabel(node);
            topology.setZEndPortLabel(zEndNodeSb.toString());
        }
    }
}
