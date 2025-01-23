package com.teliolabs.corba.data.holder;

import com.teliolabs.corba.data.dto.SNC;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class SNCHolder {

    public void setElements(Map<String, SNC> elements) {
        INSTANCE.elements = elements;
    }

    public Map<String, SNC> getElements() {
        return INSTANCE.elements;
    }

    private Map<String, SNC> elements;
    private static final SNCHolder INSTANCE = new SNCHolder();

    // Private constructor to enforce Singleton
    private SNCHolder() {
    }


    // Public method to get the instance
    public static SNCHolder getInstance() {
        return INSTANCE;
    }
}
