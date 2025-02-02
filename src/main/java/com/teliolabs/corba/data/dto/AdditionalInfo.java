package com.teliolabs.corba.data.dto;

import com.teliolabs.corba.data.types.PathType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdditionalInfo {
    private final PathType pathType;
    private final short tupleA;
    private final short tupleB;
    private final String tuple;

    public AdditionalInfo(String orderNumber) {
        tuple = orderNumber;
        String[] parts = tuple.substring(1, tuple.length() - 1).split(";");

        // Check if -1 values are at the start or end and assign remaining numbers
        if (parts[0].equals("-1") && parts[1].equals("-1")) {
            tupleA = Short.parseShort(parts[2]);
            tupleB = Short.parseShort(parts[3]);
            pathType = PathType.PROTECTION;
        } else if (parts[parts.length - 1].equals("-1") && parts[parts.length - 2].equals("-1")) {
            tupleA = Short.parseShort(parts[0]);
            tupleB = Short.parseShort(parts[1]);
            pathType = PathType.MAIN;
        } else {
            tupleA = -1;
            tupleB = -1;
            pathType = null;
        }
    }
}
