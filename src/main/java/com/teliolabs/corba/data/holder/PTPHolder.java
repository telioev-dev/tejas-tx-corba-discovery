package com.teliolabs.corba.data.holder;

import com.teliolabs.corba.data.dto.PTP;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class PTPHolder {

    public void setElements(Map<String, PTP> elements) {
        INSTANCE.elements = elements;
    }

    public Map<String, PTP> getElements() {
        return INSTANCE.elements;
    }

    private Map<String, PTP> elements;
    private static final PTPHolder INSTANCE = new PTPHolder();

    // Private constructor to enforce Singleton
    private PTPHolder() {
    }


    // Public method to get the instance
    public static PTPHolder getInstance() {
        return INSTANCE;
    }
}
