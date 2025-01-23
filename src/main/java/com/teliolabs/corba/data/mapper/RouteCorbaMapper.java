package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.TxCorbaDiscoveryApplication;
import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.utils.ManagedElementUtils;
import org.tmforum.mtnm.managedElement.ManagedElement_T;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RouteCorbaMapper implements CorbaMapper<ManagedElement_T, ManagedElement> {


    // Singleton instance
    private static final RouteCorbaMapper INSTANCE = new RouteCorbaMapper();

    // Private constructor to enforce Singleton
    private RouteCorbaMapper() {
    }

    // Public method to get the instance
    public static RouteCorbaMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public ManagedElement mapFromCorba(ManagedElement_T input) {
        String meName = ManagedElementUtils.getMEName(input.name);
        return ManagedElement.builder().
                meName(meName).
                productName(input.productName != null ? input.productName.trim() : null).
                nativeEmsName(input.nativeEMSName != null ? input.nativeEMSName.trim() : null).
                circle(ExecutionContext.getInstance().getCircle().getName()).
                ipAddress(ManagedElementUtils.getIPAddress(meName, input.additionalInfo)).
                location(input.location != null ? input.location.trim() : null).
                userLabel(input.userLabel != null ? input.userLabel.trim() : null).
                softwareVersion(input.version != null ? input.version.trim() : null).
                lastModifiedDate(TxCorbaDiscoveryApplication.NOW).build();
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
