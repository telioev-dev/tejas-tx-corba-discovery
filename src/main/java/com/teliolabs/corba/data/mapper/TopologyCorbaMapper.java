package com.teliolabs.corba.data.mapper;

import com.teliolabs.corba.TxCorbaDiscoveryApplication;
import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.dto.PTP;
import com.teliolabs.corba.data.dto.Topology;
import com.teliolabs.corba.data.holder.ManagedElementHolder;
import com.teliolabs.corba.data.holder.PTPHolder;
import com.teliolabs.corba.utils.ManagedElementUtils;
import com.teliolabs.corba.utils.PTPUtils;
import com.teliolabs.corba.utils.TopologyUtils;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.topologicalLink.TopologicalLink_T;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class TopologyCorbaMapper implements CorbaMapper<TopologicalLink_T, Topology> {
    Map<String, ManagedElement> managedElementMap = ManagedElementHolder.getInstance().getElements();
    Map<String, PTP> ptpMap = PTPHolder.getInstance().getElements();

    // Singleton instance
    private static final TopologyCorbaMapper INSTANCE = new TopologyCorbaMapper();

    // Private constructor to enforce Singleton
    private TopologyCorbaMapper() {
    }

    // Public method to get the instance
    public static TopologyCorbaMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public Topology mapFromCorba(TopologicalLink_T input) {

        log.debug("managedElementMap empty {}", (managedElementMap == null));
        log.debug("ptpMap empty {}", (ptpMap == null));

        String aEndMeName = ManagedElementUtils.getMEName(input.aEndTP);
        String zEndMeName = ManagedElementUtils.getMEName(input.zEndTP);
        String aEndPtpId = PTPUtils.getPTPName(input.aEndTP);
        String zEndPtpId = PTPUtils.getPTPName(input.zEndTP);

        ManagedElement aEndManagedElement = managedElementMap.get(aEndMeName);
        ManagedElement zEndManagedElement = managedElementMap.get(zEndMeName);

        PTP aEndPtp = ptpMap.get(aEndMeName + "_" + aEndPtpId);
        PTP zEndPtp = ptpMap.get(zEndMeName + "_" + zEndPtpId);

        log.debug("aEndManagedElement: {}, zEndManagedElement: {}, aEndPtp: {}, zEndPtp: {}",
                aEndManagedElement == null, zEndManagedElement == null, aEndPtp == null, zEndPtp == null);


        return Topology.builder().circle(ExecutionContext.getInstance().getCircle().getName()).
                nativeEmsName(input.nativeEMSName).
                aEndEms(PTPUtils.getEMSName(input.aEndTP)).
                aEndMeName(aEndMeName).
                aEndMeLabel(aEndManagedElement != null ? aEndManagedElement.getNativeEmsName() : null).
                aEndPortName(aEndPtpId).
                aEndPortLabel(aEndPtp != null ? aEndPtp.getPortNativeName() : null).
                technologyLayer(TopologyUtils.getTechnologyLayer(input.additionalInfo)).
                inconsistent(TopologyUtils.getInconsistent(input.additionalInfo)).
                direction(TopologyUtils.getLinkDirection(input)).
                linkType(TopologyUtils.getLinkType(input.additionalInfo)).
                protection(TopologyUtils.getLinkProtectionType(input.additionalInfo)).
                rate(input.rate).
                ringName(TopologyUtils.getLinkRingName(input.additionalInfo)).
                tpLinkName(TopologyUtils.getLinkName(input.name)).
                zEndMeName(zEndMeName).
                zEndMeLabel(zEndManagedElement != null ? zEndManagedElement.getNativeEmsName() : null).
                zEndEms(PTPUtils.getEMSName(input.zEndTP)).
                zEndPortLabel(zEndPtp != null ? zEndPtp.getPortNativeName() : null).userLabel(input.userLabel).
                zEndPortName(zEndPtpId).
                lastModifiedDate(TxCorbaDiscoveryApplication.NOW).build();
    }

    @Override
    public Map<String, Topology> toMap(List<Topology> list) {
        return null;
    }

    // A method to map a list of CORBA objects to DTOs
    public List<Topology> mapFromCorbaList(List<TopologicalLink_T> elementTs) {
        return elementTs.stream().map(this::mapFromCorba).collect(Collectors.toList());
    }

    public List<Topology> mapFromCorbaArray(TopologicalLink_T[] elementTs) {
        return Arrays.stream(elementTs).map(this::mapFromCorba).collect(Collectors.toList());
    }
}
