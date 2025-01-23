package com.teliolabs.corba.data.holder;

import com.teliolabs.corba.data.domain.EquipmentEntity;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class EquipmentHolder {

    public void setElements(Map<String, EquipmentEntity> elements) {
        INSTANCE.elements = elements;
    }

    public Map<String, EquipmentEntity> getElements() {
        return INSTANCE.elements;
    }

    private Map<String, EquipmentEntity> elements;
    private static final EquipmentHolder INSTANCE = new EquipmentHolder();

    // Private constructor to enforce Singleton
    private EquipmentHolder() {
    }


    // Public method to get the instance
    public static EquipmentHolder getInstance() {
        return INSTANCE;
    }
}
