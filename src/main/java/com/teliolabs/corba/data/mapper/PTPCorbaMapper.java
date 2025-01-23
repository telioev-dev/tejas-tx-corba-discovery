package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.TxCorbaDiscoveryApplication;
import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.dto.PTP;
import com.teliolabs.corba.data.holder.ManagedElementHolder;
import com.teliolabs.corba.utils.ManagedElementUtils;
import com.teliolabs.corba.utils.PTPUtils;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.terminationPoint.TerminationPoint_T;
import org.tmforum.mtnm.transmissionParameters.LayeredParameters_T;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
public class PTPCorbaMapper implements CorbaMapper<TerminationPoint_T, PTP> {


    // Singleton instance
    private static final PTPCorbaMapper INSTANCE = new PTPCorbaMapper();

    // Private constructor to enforce Singleton
    private PTPCorbaMapper() {
    }

    // Public method to get the instance
    public static PTPCorbaMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public PTP mapFromCorba(TerminationPoint_T input) {
        ZonedDateTime executionTimestamp = ExecutionContext.getInstance().getExecutionTimestamp();
        Map<String, ManagedElement> managedElementMap = ManagedElementHolder.getInstance().getElements();
        String rate = null;
        String txValue = null;
        String rxValue = null;

        if (input.transmissionParams != null) {
            for (LayeredParameters_T layeredParameter : input.transmissionParams) {
                rate = PTPUtils.deriveRate(layeredParameter);

                if (layeredParameter.transmissionParams != null) {
                    for (NameAndStringValue_T nv : layeredParameter.transmissionParams) {
                        if (nv.name.equalsIgnoreCase("TrailTraceActualTx")) {
                            txValue = nv.value;
                        } else if (nv.name.equalsIgnoreCase("TrailTraceActualRx")) {
                            rxValue = nv.value;
                        }
                    }
                }
            }
        }

        String meName = ManagedElementUtils.getMEName(input.name);
        ManagedElement managedElement = managedElementMap.get(meName);

        return PTP.builder()
                .ptpId(getPtpId(input))
                .meName(meName)
                .productName(ManagedElementUtils.getProductName(managedElement))
                .meLabel(ManagedElementUtils.getMeLabel(managedElement))
                .portLocation(getPortLocation(input))
                .portNativeName(input.nativeEMSName)
                .rate(rate)
                .slot(getSlot(input))
                .type(PTPUtils.getInterfaceType(input))
                .lastModifiedDate(executionTimestamp)
                .traceRx(rxValue)
                .traceTx(txValue)
                .build();
    }

    private String getPtpId(TerminationPoint_T input) {
        return input.name.length > 2 ? input.name[2].value : null;
    }


    private String getPortLocation(TerminationPoint_T input) {
        return (input.transmissionParams != null && input.transmissionParams.length > 0)
                ? PTPUtils.getPortLocation(input.transmissionParams[0].transmissionParams)
                : null;
    }

    private String getSlot(TerminationPoint_T input) {
        return (input.transmissionParams != null && input.transmissionParams.length > 0)
                ? PTPUtils.getLSNExtNativeLocation(input.transmissionParams[0].transmissionParams)
                : null;
    }


    @Override
    public Map<String, PTP> toMap(List<PTP> list) {
        return list.parallelStream()
                .collect(Collectors.toMap((ptp -> ptp.getMeName() + "_" + ptp.getPtpId()), obj -> obj));
    }

    // A method to map a list of CORBA objects to DTOs
    public List<PTP> mapFromCorbaList(List<TerminationPoint_T> elementTs) {
        return elementTs.stream().map(this::mapFromCorba).collect(Collectors.toList());
    }
}
