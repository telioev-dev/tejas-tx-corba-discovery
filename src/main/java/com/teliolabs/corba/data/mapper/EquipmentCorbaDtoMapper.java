package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.data.dto.Equipment;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.holder.ManagedElementHolder;
import com.teliolabs.corba.utils.EquipmentUtils;
import com.teliolabs.corba.utils.ManagedElementUtils;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.equipment.EquipmentOrHolder_T;
import org.tmforum.mtnm.equipment.Equipment_T;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Log4j2
public class EquipmentCorbaDtoMapper implements CorbaMapper<EquipmentOrHolder_T, Equipment> {


    // Singleton instance
    private static final EquipmentCorbaDtoMapper INSTANCE = new EquipmentCorbaDtoMapper();

    // Private constructor to enforce Singleton
    private EquipmentCorbaDtoMapper() {
    }

    // Public method to get the instance
    public static EquipmentCorbaDtoMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public Equipment mapFromCorba(EquipmentOrHolder_T input) {
        if (input.discriminator().value() == 0) {
            Equipment equipment = null;
            Equipment_T equipmentT = input.equip();
            ZonedDateTime executionTimestamp = ExecutionContext.getInstance().getExecutionTimestamp();
            String meName = ManagedElementUtils.getMEName(equipmentT.name);
            String location = EquipmentUtils.getSlotAddress(equipmentT.name);
            if (EquipmentUtils.isEquipmentDeleted(equipmentT.additionalInfo)) {
                log.info("EQ is marked for deletion. ME: {}, EquipmentHolder/Location: {}", meName, location);
                equipment = Equipment.builder().
                        meName(meName).
                        location(location).
                        deltaTimestamp(executionTimestamp).build();
            } else {
                log.info("EQ is marked for insert/update. ME: {}, EquipmentHolder/Location: {}", meName, location);
                Map<String, ManagedElement> managedElementMap = ManagedElementHolder.getInstance().getElements();
                ManagedElement managedElement = managedElementMap.get(meName);
                equipment = Equipment.builder().
                        meName(meName).
                        meLabel(ManagedElementUtils.getMeLabel(managedElement)).
                        expectedEquipment(equipmentT.expectedEquipmentObjectType.trim()).
                        installedEquipment(equipmentT.installedEquipmentObjectType.trim()).
                        location(EquipmentUtils.getSlotAddress(equipmentT.name)).
                        softwareVersion(equipmentT.installedVersion).
                        serialNumber(equipmentT.installedSerialNumber).
                        userLabel(equipmentT.userLabel).lastModifiedDate(executionTimestamp).
                        build();
            }
            return equipment;
        }
        return null;
    }

    @Override
    public Map<String, Equipment> toMap(List<Equipment> list) {
        return null;
    }

    @Override
    public List<Equipment> mapFromCorbaList(List<EquipmentOrHolder_T> elementTs) {
        return elementTs.stream().map(this::mapFromCorba).filter(Objects::nonNull).collect(Collectors.toList());
    }
}
