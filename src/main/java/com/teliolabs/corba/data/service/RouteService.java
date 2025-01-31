package com.teliolabs.corba.data.service;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.dto.SNC;
import com.teliolabs.corba.data.holder.SNCHolder;
import com.teliolabs.corba.data.repository.PTPRepository;
import com.teliolabs.corba.data.repository.RouteRepository;
import com.teliolabs.corba.data.types.CommunicationState;
import com.teliolabs.corba.data.types.PathType;
import com.teliolabs.corba.discovery.DiscoveryService;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.transport.CorbaErrorHandler;
import com.teliolabs.corba.utils.CorbaConstants;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.equipment.EquipmentOrHolder_T;
import org.tmforum.mtnm.equipment.Equipment_T;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;
import org.tmforum.mtnm.subnetworkConnection.CrossConnect_T;
import org.tmforum.mtnm.subnetworkConnection.Route_THolder;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

@Log4j2
public class RouteService implements DiscoveryService {

    private static RouteService instance;
    private final RouteRepository routeRepository;
    private NameAndStringValue_T[] sncNameArray;
    private Integer discoveryCount = 0;
    private long start;
    private long end;

    // Public method to get the singleton instance
    public static RouteService getInstance(RouteRepository routeRepository) {
        if (instance == null) {
            synchronized (RouteService.class) {
                if (instance == null) {
                    instance = new RouteService(routeRepository);
                    log.debug("RouteService instance created.");
                }
            }
        }
        return instance;
    }

    private RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
        DiscoveryItemType discoveryItemType = ExecutionContext.getInstance().getEntity();
        if (DiscoveryItemType.ROUTE == discoveryItemType) {
            boolean isDelta = isExecutionModeDelta();
            sncNameArray = new NameAndStringValue_T[isDelta ? 4 : 3];
            sncNameArray[0] = new NameAndStringValue_T(CorbaConstants.EMS_STR, ExecutionContext.getInstance().getCircle().getEms().replace(":", "/"));
            sncNameArray[1] = new NameAndStringValue_T(CorbaConstants.MULTILAYER_SUBNETWORK_STR, "0001");
            sncNameArray[2] = new NameAndStringValue_T();
            sncNameArray[2].name = CorbaConstants.SUBNETWORK_CONNECTION_STR;
            if (isDelta) {
                sncNameArray[3] = new NameAndStringValue_T(CorbaConstants.TIMESTAMP_SIGNATURE_STR, ExecutionContext.getInstance().getDeltaTimestamp());
            }
        }
    }


    public void discoverRoutes(CorbaConnection corbaConnection) throws Exception {
        Map<String, SNC> subnetworkConnectionsMap = SNCHolder.getInstance().getElements();
        start = System.currentTimeMillis();
        if (subnetworkConnectionsMap != null && !subnetworkConnectionsMap.isEmpty()) {
            Set<String> sncIds = subnetworkConnectionsMap.keySet();
            int i = 0;
            for (String sncId : sncIds) {
                SNC snc = subnetworkConnectionsMap.get(sncId);
                log.info("#{} SNC '{}' for getRoute processing.", i++, sncId);
                try {
                    invokeAndProcessGetRouteSNC(sncId, corbaConnection);
                } catch (ProcessingFailureException e) {
                    if (e.errorReason.contains("ENTITY_NOT_FOUND")) {
                        log.error("SNC: {} getRoute didn't succeed: ENTITY_NOT_FOUND ", sncId);
                        continue;
                    }
                    CorbaErrorHandler.handleProcessingFailureException(e, "SNC: " + sncId + "getRoute");
                    end = System.currentTimeMillis();
                    printDiscoveryResult(end - start);
                    throw e;
                }
            }
        }
        end = System.currentTimeMillis();
        printDiscoveryResult(end - start);
        updateJobStatus();
    }

    private void invokeAndProcessGetRouteSNC(String sncId, CorbaConnection corbaConnection) throws ProcessingFailureException {
        sncNameArray[2].value = sncId;
        Route_THolder routeHolder = new Route_THolder();
        try {
            corbaConnection.getMlsnManager().getRoute(sncNameArray, false, routeHolder);
        } catch (ProcessingFailureException e) {
            CorbaErrorHandler.handleProcessingFailureException(e, "getRoute. SNC: " + sncId);
            throw e;
        }
        discoveryCount = discoveryCount + routeHolder.value.length;
        log.info("getRoute: got " + routeHolder.value.length + " Cross-connects for SNC " + sncId);
        log.info("getRoute: total Cross-connects {}", discoveryCount);
        int i = 1;
//        for (CrossConnect_T crossConnect : routeHolder.value) {
//            log.info("Logging Cross Connect ({}) details for SNC: {}", i, sncId);
//            logCrossConnectDetails(crossConnect);
//            i++;
//        }
    }

    private void logCrossConnectDetails(CrossConnect_T crossConnectT) {
        log.info("Active: {}", crossConnectT.active);
        log.info("CC Type: {}", crossConnectT.ccType.value());
        log.info("Direction: {}", crossConnectT.direction.value());
        log.info("CC Additional Info Size: {}", crossConnectT.additionalInfo.length);
        log.info("CC aEndNameList Size: {}", crossConnectT.aEndNameList.length);
        log.info("CC zEndNameList Size: {}", crossConnectT.zEndNameList.length);
        Arrays.stream(crossConnectT.additionalInfo).filter(nameAndStringValueT ->
                nameAndStringValueT.name.equals("OrderNumber") || nameAndStringValueT.name.equals("PathType")).forEach(attr -> {
            if (attr.name.equals("PathType")) {
                log.info("CC Additional Info {}: {}", attr.name, PathType.fromCode(Integer.parseInt(attr.value)));
            } else {
                log.info("CC Additional Info {}: {}", attr.name, attr.value);
            }

        });

        Arrays.stream(crossConnectT.aEndNameList).forEach(attr -> {
            Arrays.stream(attr).filter(atttr -> atttr.name.equals("ManagedElement") || atttr.name.equals("PTP") || atttr.name.equals("CTP")).forEach(attrr ->
                    log.info("aEndNameList - {}: {}", attrr.name, attrr.value));
        });
        Arrays.stream(crossConnectT.aEndNameList).forEach(attr -> {
            Arrays.stream(attr).filter(atttr -> atttr.name.equals("ManagedElement") || atttr.name.equals("PTP") || atttr.name.equals("CTP")).forEach(attrr ->
                    log.info("zEndNameList - {}: {}", attrr.name, attrr.value));
        });

    }

    public void runDeltaProcess(CorbaConnection corbaConnection) {

    }

    @Override
    public int discover(CorbaConnection corbaConnection) {
        return 0;
    }

    @Override
    public int discoverDelta(CorbaConnection corbaConnection) {
        return 0;
    }

    @Override
    public int deleteAll() {
        return routeRepository.truncate();
    }

    @Override
    public int getDiscoveryCount() {
        return discoveryCount;
    }

    @Override
    public long getStartDiscoveryTimestampInMillis() {
        return start;
    }

    @Override
    public long getEndDiscoveryTimestampInMillis() {
        return end;
    }

    @Override
    public DiscoveryItemType getDiscoveryItemType() {
        return DiscoveryItemType.ROUTE;
    }
}
