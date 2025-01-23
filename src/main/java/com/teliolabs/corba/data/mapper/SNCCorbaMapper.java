package com.teliolabs.corba.data.mapper;

import com.teliolabs.corba.TxCorbaDiscoveryApplication;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.dto.PTP;
import com.teliolabs.corba.data.dto.SNC;
import com.teliolabs.corba.data.dto.Topology;
import com.teliolabs.corba.data.holder.ManagedElementHolder;
import com.teliolabs.corba.data.holder.PTPHolder;
import com.teliolabs.corba.utils.ManagedElementUtils;
import com.teliolabs.corba.utils.PTPUtils;
import com.teliolabs.corba.utils.SNCUtils;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.subnetworkConnection.SubnetworkConnection_T;
import org.tmforum.mtnm.subnetworkConnection.TPData_T;
import org.tmforum.mtnm.topologicalLink.TopologicalLink_T;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class SNCCorbaMapper implements CorbaMapper<SubnetworkConnection_T, SNC> {

    Map<String, ManagedElement> managedElementMap = ManagedElementHolder.getInstance().getElements();
    Map<String, PTP> ptpMap = PTPHolder.getInstance().getElements();

    // Singleton instance
    private static final SNCCorbaMapper INSTANCE = new SNCCorbaMapper();

    // Private constructor to enforce Singleton
    private SNCCorbaMapper() {
    }

    // Public method to get the instance
    public static SNCCorbaMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public SNC mapFromCorba(SubnetworkConnection_T input) {
        String sncId = SNCUtils.getSNCId(input);
        String sncName = SNCUtils.getSNCUserLabel(input);
        String circuitId = SNCUtils.getCircuitId(sncName);
        String srfId = SNCUtils.getSRFId(circuitId);

        log.debug("aEnd Size : " + input.aEnd.length);
        log.debug("zEnd Size : " + input.zEnd.length);

        TPData_T[] aEndTpData = input.aEnd;
        TPData_T[] zEndTpData = input.zEnd;
        String aEndMe = null;
        String aEndPtpId = null;
        String aEndCtpName = null;

        String zEndMe = null;
        String zEndPtpId = null;
        String zEndCtpName = null;

        if (aEndTpData.length >= 1) {
            int index = aEndTpData.length == 2 ? 1 : 0;
            aEndMe = ManagedElementUtils.getMEName(aEndTpData[index].tpName);
            aEndPtpId = PTPUtils.getPTPName(aEndTpData[index].tpName);
            aEndCtpName = PTPUtils.getCTPName(aEndTpData[index].tpName);
        }

        if (zEndTpData.length >= 1) {
            int index = zEndTpData.length == 2 ? 1 : 0;
            zEndMe = ManagedElementUtils.getMEName(zEndTpData[index].tpName);
            zEndPtpId = PTPUtils.getPTPName(zEndTpData[index].tpName);
            zEndCtpName = PTPUtils.getCTPName(zEndTpData[index].tpName);
        }


        ManagedElement aEndManagedElement = managedElementMap.get(aEndMe);
        ManagedElement zEndManagedElement = managedElementMap.get(zEndMe);

        PTP aEndPtp = ptpMap.get(aEndMe + "_" + aEndPtpId);
        PTP zEndPtp = ptpMap.get(zEndMe + "_" + zEndPtpId);

        log.debug("aEndManagedElement: {}, zEndManagedElement: {}, aEndPtp: {}, zEndPtp: {}",
                aEndManagedElement == null, zEndManagedElement == null, aEndPtp == null, zEndPtp == null);


        return SNC.builder().
                aEndChannel("").
                aEndMe(aEndMe).
                aEndMeLabel(aEndManagedElement != null ? aEndManagedElement.getNativeEmsName() : null).
                aEndPtp(aEndPtpId).
                aEndPtpLabel(aEndPtp != null ? aEndPtp.getPortNativeName() : null).
                sncId(sncId).
                zEndChannel("").
                zEndMe(zEndMe).
                zEndMeLabel(zEndManagedElement != null ? zEndManagedElement.getNativeEmsName() : null).
                zEndPtp(zEndPtpId).
                zEndPtpLabel(zEndPtp != null ? zEndPtp.getPortNativeName() : null).
                sncName(sncName).
                sncRate(input.rate).
                circuitId(circuitId == null ? null : Long.parseLong(circuitId)).
                vCat(SNCUtils.getVCat(input)).
                srfId(srfId == null ? null : Integer.parseInt(srfId)).
                lastModifiedDate(TxCorbaDiscoveryApplication.NOW).build();
    }

    @Override
    public Map<String, SNC> toMap(List<SNC> list) {
        return null;
    }

    // A method to map a list of CORBA objects to DTOs
    public List<SNC> mapFromCorbaList(List<SubnetworkConnection_T> elementTs) {
        return elementTs.stream().map(this::mapFromCorba).collect(Collectors.toList());
    }

    // A method to map a list of CORBA objects to DTOs
    public List<SNC> mapFromCorbaArray(SubnetworkConnection_T [] elementTs) {
        return Arrays.stream(elementTs).map(this::mapFromCorba).collect(Collectors.toList());
    }
}
