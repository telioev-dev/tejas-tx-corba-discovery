package com.teliolabs.corba.utils;

import java.util.HashMap;
import java.util.Map;

public class RateUtil {
    private static final Map<Integer, String> rateMap = new HashMap<>();

    static {
        // STM rates
        rateMap.put(73, "STM1");
        rateMap.put(74, "STM4");
        rateMap.put(75, "STM8");
        rateMap.put(76, "STM16");
        rateMap.put(77, "STM64");
        rateMap.put(78, "STM256");

        // E-rates
        rateMap.put(5, "E1");
        rateMap.put(7, "E3");

        // Other rates
        rateMap.put(48, "IF");
        rateMap.put(61, "FE");
        rateMap.put(97, "FE");
        rateMap.put(87, "GigE");
        rateMap.put(68, "GigE");
        rateMap.put(113, "10GigE");
        rateMap.put(316, "100GigE");
        rateMap.put(96, "Ethernet");

        // VC rates
        rateMap.put(11, "VC12");
        rateMap.put(296, "VC3");
        rateMap.put(15, "VC4");

        // WDM rates
        rateMap.put(47, "WDM");
        rateMap.put(40, "WDM");
        rateMap.put(49, "WDM");
        rateMap.put(41, "OMS");
        rateMap.put(42, "OTS");
    }

    public static String getRate(int rate) {
        return rateMap.getOrDefault(rate, "N.A");
    }
}

