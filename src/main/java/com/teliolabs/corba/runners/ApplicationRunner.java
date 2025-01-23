package com.teliolabs.corba.runners;

import java.util.Map;

@FunctionalInterface
public interface ApplicationRunner {
    void run(Map<String, String> args);
}
