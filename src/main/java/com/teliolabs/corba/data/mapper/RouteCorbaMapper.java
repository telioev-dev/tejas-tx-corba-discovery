package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.data.domain.RouteEntity;
import com.teliolabs.corba.data.dto.AdditionalInfo;
import com.teliolabs.corba.data.dto.End;
import com.teliolabs.corba.data.dto.Route;
import com.teliolabs.corba.data.dto.SNC;
import com.teliolabs.corba.data.holder.SNCHolder;
import com.teliolabs.corba.utils.JklmUtils;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.subnetworkConnection.CrossConnect_T;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class RouteCorbaMapper {


    // Singleton instance
    private static final RouteCorbaMapper INSTANCE = new RouteCorbaMapper();

    // Private constructor to enforce Singleton
    private RouteCorbaMapper() {
    }

    // Public method to get the instance
    public static RouteCorbaMapper getInstance() {
        return INSTANCE;
    }

    public RouteEntity mapFromCorba(CrossConnect_T crossConnectT, SNC associatedSnc) {
        ZonedDateTime executionTimestamp = ExecutionContext.getInstance().getExecutionTimestamp();
        Map<String, SNC> sncMap = SNCHolder.getInstance().getElements();
        return null;
    }


    public Map<String, Route> toMap(List<Route> list) {
        return null;
    }

    public List<RouteEntity> mapFromCorbaList(List<CrossConnect_T> crossConnectTList, SNC associatedSnc) {
        return crossConnectTList.stream().
                map(crossConnectT -> mapFromCorba(crossConnectT, associatedSnc)).collect(Collectors.toList());
    }

    public List<RouteEntity> mapFromCorbaArray(CrossConnect_T[] crossConnectTList, SNC associatedSnc) {
        return Arrays.stream(crossConnectTList).
                map(crossConnectT -> mapFromCorba(crossConnectT, associatedSnc)).collect(Collectors.toList());
    }

    private List<End> createEndList(NameAndStringValue_T[][] nameAndStringValueArray, String rate) {
        List<End> endList = new ArrayList<>();
        for (NameAndStringValue_T[] nvArray : nameAndStringValueArray) {
            End end = new End();
            endList.add(end);
            for (NameAndStringValue_T nv : nvArray) {
                if (nv.name.equalsIgnoreCase("PTP")) {
                    end.setPtp(nv.value);
                } else if (nv.name.equalsIgnoreCase("CTP")) {
                    end.setCtp(JklmUtils.extract(nv.value, String.valueOf(rate)));
                } else if (nv.name.equalsIgnoreCase("ManagedElement")) {
                    end.setManagedElement(nv.value);
                }
            }
        }
        return endList;
    }

    private void processCrossConnect(CrossConnect_T crossConnectT, SNC snc) {
        short rate = snc.getSncRate();
        int additionalInfoLen = crossConnectT.additionalInfo.length;
        List<AdditionalInfo> additionalInfos = new ArrayList<>();
        for (int i = 0; i < additionalInfoLen; i++) {
            if (crossConnectT.additionalInfo[i].name.equalsIgnoreCase("OrderNumber")) {
                additionalInfos.add(new AdditionalInfo(crossConnectT.additionalInfo[i].value));
            }
        }

        log.info("raw additionalInfoLen : {}", additionalInfoLen);
        log.info("AdditionalInfo List size: {}", additionalInfos.size());

        log.info("aEndNameList: {}", crossConnectT.aEndNameList.length);
        List<End> aEndList = createEndList(crossConnectT.aEndNameList, String.valueOf(rate));
        log.info("aEndList: {}", aEndList.size());

        log.info("zEndNameList: {}", crossConnectT.zEndNameList.length);
        List<End> zEndList = createEndList(crossConnectT.zEndNameList, String.valueOf(rate));
        log.info("zEndList: {}", zEndList.size());
    }
}
