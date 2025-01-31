package com.teliolabs.corba.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JklmUtils {

    public static String extract(String str, String rate) {
        if (rate.contains("WDM")) {
            Matcher matcher = Pattern.compile("/frequency=(\\d+\\.\\d+)").matcher(str);
            return matcher.find() ? (int) (Double.parseDouble(matcher.group(1)) * 100) % 10000 + "-0-0-0" : str;
        }

        Map<String, Integer> values = new HashMap<>();
        List<String> keys = Arrays.asList("j", "k", "l", "m");

        for (String key : keys) {
            Matcher matcher = Pattern.compile(key + "=(\\d+)").matcher(str);
            values.put(key, matcher.find() ? Integer.parseInt(matcher.group(1)) : 0);
        }

        return values.values().stream().allMatch(v -> v == 0)
                ? str
                : values.get("j") + "-" + values.get("k") + "-" + values.get("l") + "-" + values.get("m");
    }
}
