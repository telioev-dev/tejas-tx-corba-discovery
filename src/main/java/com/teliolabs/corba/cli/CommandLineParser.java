package com.teliolabs.corba.cli;

import java.util.HashMap;
import java.util.Map;


public class CommandLineParser {

    private final Map<String, String> argsMap = new HashMap<>();

    public Map<String, String> getArgs() {
        return argsMap;
    }

    public CommandLineParser(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] keyValue = arg.substring(2).split("=", 2);
                if (keyValue.length == 2) {
                    argsMap.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    public String get(String key) {
        return argsMap.get(key);
    }

    public String getOrDefault(String key, String defaultValue) {
        return argsMap.getOrDefault(key, defaultValue);
    }

    public String get(CommandLineArg commandArg) {
        return argsMap.get(commandArg.getKey());
    }

    public String getOrDefault(CommandLineArg commandArg, String defaultValue) {
        return argsMap.getOrDefault(commandArg.getKey(), defaultValue);
    }

    @Override
    public String toString() {
        return String.join(", ", argsMap.toString());
    }
}

