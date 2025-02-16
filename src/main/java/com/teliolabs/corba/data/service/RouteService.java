package com.teliolabs.corba.data.service;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.domain.RouteEntity;
import com.teliolabs.corba.data.dto.AdditionalInfo;
import com.teliolabs.corba.data.dto.End;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.dto.SNC;
import com.teliolabs.corba.data.holder.SNCHolder;
import com.teliolabs.corba.data.mapper.RouteCorbaMapper;
import com.teliolabs.corba.data.repository.PTPRepository;
import com.teliolabs.corba.data.repository.RouteRepository;
import com.teliolabs.corba.data.types.CommunicationState;
import com.teliolabs.corba.data.types.PathType;
import com.teliolabs.corba.discovery.DiscoveryService;
import com.teliolabs.corba.exception.InvalidArgumentException;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.transport.CorbaErrorHandler;
import com.teliolabs.corba.utils.CorbaConstants;
import com.teliolabs.corba.utils.JklmUtils;
import com.teliolabs.corba.utils.RouteUtils;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.equipment.EquipmentOrHolder_T;
import org.tmforum.mtnm.equipment.Equipment_T;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;
import org.tmforum.mtnm.subnetworkConnection.CrossConnect_T;
import org.tmforum.mtnm.subnetworkConnection.Route_THolder;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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
        if (DiscoveryItemType.ROUTE == discoveryItemType || DiscoveryItemType.ROUTE_PACKET == discoveryItemType || DiscoveryItemType.SNC == discoveryItemType || DiscoveryItemType.SNC_PACKET == discoveryItemType) {
            sncNameArray = new NameAndStringValue_T[3];
            sncNameArray[0] = new NameAndStringValue_T(CorbaConstants.EMS_STR, ExecutionContext.getInstance().getCircle().getEms().replace(":", "/"));
            sncNameArray[1] = new NameAndStringValue_T(CorbaConstants.MULTILAYER_SUBNETWORK_STR, "0001");
            sncNameArray[2] = new NameAndStringValue_T();
            sncNameArray[2].name = CorbaConstants.SUBNETWORK_CONNECTION_STR;
        }
    }


    public void discoverRoutes(CorbaConnection corbaConnection) throws Exception {
        Map<String, SNC> subnetworkConnectionsMap = SNCHolder.getInstance().getElements();
        start = System.currentTimeMillis();
        if (subnetworkConnectionsMap != null && !subnetworkConnectionsMap.isEmpty()) {
            log.info("SNCs to run getRoute for: - {}", subnetworkConnectionsMap.size());
            Set<String> sncIds = subnetworkConnectionsMap.keySet();
            String entityId = ExecutionContext.getInstance().getEntityId();
            if (entityId != null && !entityId.isEmpty()) {
                log.info("Invoking getRoute for specific SNC ID: {}", entityId);
                SNC snc = subnetworkConnectionsMap.get(entityId);
                if (snc != null) {
                    invokeAndProcessGetRouteSNC(snc, corbaConnection);
                } else {
                    throw new InvalidArgumentException("SNC: '" + entityId + "' not found, unable to invoke getRoute.");
                }
            } else {
                int i = 0;
                for (String sncId : sncIds) {
                    SNC snc = subnetworkConnectionsMap.get(sncId);
                    log.info("#{} SNC '{}' for getRoute processing.", i++, sncId);
                    try {
                        invokeAndProcessGetRouteSNC(snc, corbaConnection);
                    } catch (ProcessingFailureException e) {
                        if (e.errorReason.contains("ENTITY_NOT_FOUND")) {
                            log.error("SNC: {} getRoute didn't succeed: ENTITY_NOT_FOUND ", sncId);
                            continue;
                        }
                        CorbaErrorHandler.handleProcessingFailureException(e, "SNC: " + sncId + "getRoute");
                        end = System.currentTimeMillis();
                        printDiscoveryResult(end - start);
                        throw e;
                    } catch (org.omg.CORBA.COMM_FAILURE e) {
                        log.error("COMM_FAILURE occurred during getRoute ");
                        e.printStackTrace();
                    } catch (org.omg.CORBA.BAD_PARAM e) {
                        log.error("BAD_PARAM occurred during getRoute ");
                        e.printStackTrace();
                    }
                }
            }
        }
        end = System.currentTimeMillis();
        printDiscoveryResult(end - start);
        updateJobStatus();
    }

    private void invokeAndProcessGetRouteSNC(SNC snc, CorbaConnection corbaConnection) throws ProcessingFailureException, SQLException {
        String sncId = snc.getSncId();
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
        List<RouteEntity> routeEntities = new ArrayList<>();
        for (CrossConnect_T crossConnect : routeHolder.value) {
            if (log.isDebugEnabled()) {
                logCrossConnectDetails(crossConnect);
            }
            routeEntities.addAll(processCrossConnect(crossConnect, snc));
        }

        if (!routeEntities.isEmpty()) {
            log.debug("Routes count {} to be inserted for SNC : {}", routeEntities.size(), sncId);
            routeRepository.insertRoutes(routeEntities, 50);
            log.debug("Total routes {} inserted in DB for SNC: {}", routeEntities.size(), sncId);
        }
        log.info("Cross-Connect processing done for SNC: {}", sncId);
    }

    private List<RouteEntity> processCrossConnect(CrossConnect_T crossConnectT, SNC snc) throws SQLException {
        short rate = snc.getSncRate();
        int additionalInfoLen = crossConnectT.additionalInfo.length;
        List<AdditionalInfo> additionalInfos = new ArrayList<>();
        for (int i = 0; i < additionalInfoLen; i++) {
            if (crossConnectT.additionalInfo[i].name.equalsIgnoreCase("OrderNumber")) {
                additionalInfos.add(new AdditionalInfo(crossConnectT.additionalInfo[i].value));
            }
        }

        log.debug("raw additionalInfoLen : {}", additionalInfoLen);
        log.debug("AdditionalInfo List size: {}", additionalInfos.size());

        log.debug("aEndNameList: {}", crossConnectT.aEndNameList.length);
        List<End> aEndList = createEndList(crossConnectT.aEndNameList, String.valueOf(rate));
        log.debug("aEndList: {}", aEndList.size());

        log.debug("zEndNameList: {}", crossConnectT.zEndNameList.length);
        List<End> zEndList = createEndList(crossConnectT.zEndNameList, String.valueOf(rate));
        log.debug("zEndList: {}", zEndList.size());

        List<RouteEntity> routeEntities = new ArrayList<>();
        if ((!aEndList.isEmpty() && !zEndList.isEmpty()) && (aEndList.size() == zEndList.size())) {
            for (int i = 0; i < aEndList.size(); i++) {
                End aEnd = aEndList.get(i);
                End zEnd = zEndList.get(i);
                AdditionalInfo additionalInfo = additionalInfos.get(i);
                RouteEntity routeEntity = RouteEntity.builder().
                        sncId(snc.getSncId()).
                        sncName(snc.getSncName()).
                        aEndMe(aEnd.getManagedElement()).
                        aEndPtp(aEnd.getPtp()).
                        aEndCtp(aEnd.getCtp()).
                        zEndMe(zEnd.getManagedElement()).
                        zEndPtp(zEnd.getPtp()).
                        zEndCtp(zEnd.getCtp()).
                        pathType(additionalInfo.getPathType()).tuple(additionalInfo.getTuple()).
                        tupleA(additionalInfo.getTupleA()).
                        tupleB(additionalInfo.getTupleB()).build();
                log.debug("Route: {}", routeEntity);
                routeEntities.add(routeEntity);
            }
        } else if (aEndList.isEmpty() && !zEndList.isEmpty()) {
            for (int i = 0; i < zEndList.size(); i++) {
                End zEnd = zEndList.get(i);
                AdditionalInfo additionalInfo = additionalInfos.get(i);
                RouteEntity routeEntity = RouteEntity.builder().
                        sncId(snc.getSncId()).
                        sncName(snc.getSncName()).
                        zEndMe(zEnd.getManagedElement()).
                        zEndPtp(zEnd.getPtp()).
                        zEndCtp(zEnd.getCtp()).
                        pathType(additionalInfo.getPathType()).
                        tupleA(additionalInfo.getTupleA()).tuple(additionalInfo.getTuple()).
                        tupleB(additionalInfo.getTupleB()).build();
                log.debug("Route: {}", routeEntity);
                routeEntities.add(routeEntity);
            }
        } else if (!aEndList.isEmpty() && zEndList.isEmpty()) {
            for (int i = 0; i < aEndList.size(); i++) {
                End aEnd = aEndList.get(i);
                AdditionalInfo additionalInfo = additionalInfos.get(i);
                RouteEntity routeEntity = RouteEntity.builder().
                        sncId(snc.getSncId()).
                        sncName(snc.getSncName()).
                        aEndMe(aEnd.getManagedElement()).
                        aEndPtp(aEnd.getPtp()).
                        aEndCtp(aEnd.getCtp()).
                        pathType(additionalInfo.getPathType()).
                        tupleA(additionalInfo.getTupleA()).tuple(additionalInfo.getTuple()).
                        tupleB(additionalInfo.getTupleB()).build();
                log.debug("Route: {}", routeEntity);
                routeEntities.add(routeEntity);
            }
        }
        return routeEntities;
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

    private void logCrossConnectDetails(CrossConnect_T crossConnectT) {
        log.debug("Active: {}", crossConnectT.active);
        log.debug("CC Type: {}", crossConnectT.ccType.value());
        log.debug("Direction: {}", crossConnectT.direction.value());
        log.debug("CC Additional Info Size: {}", crossConnectT.additionalInfo.length);
        log.debug("CC aEndNameList Size: {}", crossConnectT.aEndNameList.length);
        log.debug("CC zEndNameList Size: {}", crossConnectT.zEndNameList.length);
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
}
