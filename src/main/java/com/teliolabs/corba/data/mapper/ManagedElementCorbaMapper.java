package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.types.CommunicationState;
import com.teliolabs.corba.utils.ManagedElementUtils;
import org.tmforum.mtnm.managedElement.ManagedElement_T;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManagedElementCorbaMapper implements CorbaMapper<ManagedElement_T, ManagedElement> {


    // Singleton instance
    private static final ManagedElementCorbaMapper INSTANCE = new ManagedElementCorbaMapper();

    // Private constructor to enforce Singleton
    private ManagedElementCorbaMapper() {
    }

    // Public method to get the instance
    public static ManagedElementCorbaMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public ManagedElement mapFromCorba(ManagedElement_T input) {
        ZonedDateTime executionTimestamp = ExecutionContext.getInstance().getExecutionTimestamp();
        String meName = ManagedElementUtils.getMEName(input.name);
        return ManagedElement.builder().
                meName(meName).
                communicationState(CommunicationState.fromState(input.communicationState.value())).
                productName(input.productName != null ? input.productName.trim() : null).
                nativeEmsName(input.nativeEMSName != null ? input.nativeEMSName.trim() : null).
                circle(ExecutionContext.getInstance().getCircle().getName()).
                ipAddress(ManagedElementUtils.getIPAddress(meName, input.additionalInfo)).
                location(input.location != null ? input.location.trim() : null).
                userLabel(input.userLabel != null ? input.userLabel.trim() : null).
                softwareVersion(input.version != null ? input.version.trim() : null).
                lastModifiedDate(executionTimestamp).build();
    }

    @Override
    public Map<String, ManagedElement> toMap(List<ManagedElement> list) {
        return list.parallelStream()
                .collect(Collectors.toMap(ManagedElement::getMeName, obj -> obj));
    }

    // A method to map a list of CORBA objects to DTOs
    public List<ManagedElement> mapFromCorbaList(List<ManagedElement_T> elementTs) {
        return elementTs.stream().map(this::mapFromCorba).collect(Collectors.toList());
    }
}
