package com.teliolabs.corba.data.holder;

import com.teliolabs.corba.data.dto.ManagedElement;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class ManagedElementHolder {

    public void setElements(Map<String, ManagedElement> elements) {
        INSTANCE.elements = elements;
    }

    public Map<String, ManagedElement> getElements() {
        return INSTANCE.elements;
    }

    private Map<String, ManagedElement> elements;
    private static final ManagedElementHolder INSTANCE = new ManagedElementHolder();

    // Private constructor to enforce Singleton
    private ManagedElementHolder() {
    }


    // Public method to get the instance
    public static ManagedElementHolder getInstance() {
        return INSTANCE;
    }
}
