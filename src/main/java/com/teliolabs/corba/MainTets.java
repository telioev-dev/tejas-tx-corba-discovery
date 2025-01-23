package com.teliolabs.corba;

import com.teliolabs.corba.utils.SNCUtils;

import java.time.ZonedDateTime;

public class MainTets {

    public static void main(String args[]) {

        String test = "EXT-DLCZ-STD-26101721652Z-20MB-1ZZ-FIB-ELE_ELE-SIFY_TECHNOLOGIES-910086Z-SRT_GANDHIPALACE_BG_To_sify_srtZZZZZZZZZ-270122";

        String circuitIdStr = SNCUtils.getCircuitId(test);
        System.out.println("Circuit ID: " + circuitIdStr);
        System.out.println(Long.parseLong(circuitIdStr));
        System.out.println(ZonedDateTime.now());

        System.out.println(ZonedDateTime.parse("2025-01-23T16:27:25.429+05:30"));

    }

}
