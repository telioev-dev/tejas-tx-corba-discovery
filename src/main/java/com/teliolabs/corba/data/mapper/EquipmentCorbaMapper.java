package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.data.domain.EquipmentEntity;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.holder.ManagedElementHolder;
import com.teliolabs.corba.utils.EquipmentUtils;
import com.teliolabs.corba.utils.ManagedElementUtils;
import org.tmforum.mtnm.equipment.EquipmentOrHolder_T;
import org.tmforum.mtnm.equipment.Equipment_T;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EquipmentCorbaMapper implements CorbaMapper<EquipmentOrHolder_T, EquipmentEntity> {


    // Singleton instance
    private static final EquipmentCorbaMapper INSTANCE = new EquipmentCorbaMapper();

    // Private constructor to enforce Singleton
    private EquipmentCorbaMapper() {
    }

    // Public method to get the instance
    public static EquipmentCorbaMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public EquipmentEntity mapFromCorba(EquipmentOrHolder_T input) {

        if (input.discriminator().value() == 0) {
            ZonedDateTime executionTimestamp = ExecutionContext.getInstance().getExecutionTimestamp();
            Map<String, ManagedElement> managedElementMap = ManagedElementHolder.getInstance().getElements();
            Equipment_T equipment = input.equip();
            String meName = ManagedElementUtils.getMEName(equipment.name);
            ManagedElement managedElement = managedElementMap.get(meName);

            EquipmentEntity equipmentEntity = EquipmentEntity.builder().
                    meName(meName).
                    meLabel(ManagedElementUtils.getMeLabel(managedElement)).
                    expectedEquipment(equipment.expectedEquipmentObjectType.trim()).
                    installedEquipment(equipment.installedEquipmentObjectType.trim()).
                    location(EquipmentUtils.getSlotAddress(equipment.name)).
                    softwareVersion(equipment.installedVersion).
                    serialNumber(equipment.installedSerialNumber).
                    userLabel(equipment.userLabel).lastModifiedDate(executionTimestamp).
            build();
            return equipmentEntity;
        }
        return null;
    }

    @Override
    public Map<String, EquipmentEntity> toMap(List<EquipmentEntity> list) {
        return null;
    }

    @Override
    public List<EquipmentEntity> mapFromCorbaList(List<EquipmentOrHolder_T> elementTs) {
        return elementTs.stream().map(this::mapFromCorba).collect(Collectors.toList());
    }
}
