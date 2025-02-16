package com.teliolabs.corba.data.service;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.dto.SNC;
import com.teliolabs.corba.data.holder.SNCHolder;
import com.teliolabs.corba.data.mapper.SNCCorbaMapper;
import com.teliolabs.corba.data.mapper.SNCResultSetMapper;
import com.teliolabs.corba.data.repository.RouteRepository;
import com.teliolabs.corba.data.repository.SNCRepository;
import com.teliolabs.corba.discovery.DiscoveryService;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.transport.CorbaErrorHandler;
import com.teliolabs.corba.utils.CollectionUtils;
import com.teliolabs.corba.utils.CorbaConstants;
import com.teliolabs.corba.utils.SNCUtils;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;
import org.tmforum.mtnm.multiLayerSubnetwork.MultiLayerSubnetwork_T;
import org.tmforum.mtnm.multiLayerSubnetwork.SubnetworkIterator_I;
import org.tmforum.mtnm.multiLayerSubnetwork.SubnetworkIterator_IHolder;
import org.tmforum.mtnm.multiLayerSubnetwork.SubnetworkList_THolder;
import org.tmforum.mtnm.subnetworkConnection.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class SNCService implements DiscoveryService {

    private static SNCService instance;
    private final SNCRepository sncRepository;
    private List<SubnetworkConnection_T> subnetworkConnectionTList = new ArrayList<>();
    private Integer discoveryCount = 0;
    private long start;
    private long end;


    // Public method to get the singleton instance
    public static SNCService getInstance(SNCRepository sncRepository) {
        if (instance == null) {
            synchronized (SNCService.class) {
                if (instance == null) {
                    instance = new SNCService(sncRepository);
                    log.debug("SNCService instance created.");
                }
            }
        }
        return instance;
    }

    private SNCService(SNCRepository sncRepository) {
        this.sncRepository = sncRepository;
    }

    private NameAndStringValue_T[] buildDeltaSearchCriteria(MultiLayerSubnetwork_T subnetwork) {
        if (subnetwork == null || subnetwork.name == null || subnetwork.name.length == 0) {
            log.warn("Subnetwork is null or has no name attributes");
            return new NameAndStringValue_T[0];
        }

        log.info("Building delta search criteria for SNC discovery.");
        String emsName = null;
        String subnetworkName = null;

        for (NameAndStringValue_T s : subnetwork.name) {
            if (CorbaConstants.EMS_STR.equalsIgnoreCase(s.name)) {
                emsName = s.value;
            } else if (CorbaConstants.MULTILAYER_SUBNETWORK_STR.equalsIgnoreCase(s.name)) {
                log.info("s.value " + s.value);
                subnetworkName = s.value;
            }
        }

        return new NameAndStringValue_T[]{
                new NameAndStringValue_T(CorbaConstants.EMS_STR, emsName),
                new NameAndStringValue_T(CorbaConstants.MULTILAYER_SUBNETWORK_STR, subnetworkName),
                new NameAndStringValue_T(CorbaConstants.TIMESTAMP_SIGNATURE_STR, ExecutionContext.getInstance().getDeltaTimestamp())
        };
    }

    public void discoverSubnetworkConnections(CorbaConnection corbaConnection, ExecutionMode executionMode) throws SQLException, ProcessingFailureException {

        try {
            List<MultiLayerSubnetwork_T> subnetworks = fetchAllSubnetworks(corbaConnection);
            for (MultiLayerSubnetwork_T subnetwork : subnetworks) {
                logSubnetworkDetails(subnetwork);
                processSubnetwork(subnetwork, corbaConnection);
            }
            updateJobStatus();
            if (subnetworkConnectionTList != null && !subnetworkConnectionTList.isEmpty() && executionMode == ExecutionMode.IMPORT) {
                log.info("Total SNCs on all Subnetworks discovered from NMS: {}", subnetworkConnectionTList.size());
                saveSubnetworkConnections();
            }

        } catch (Exception e) {
            log.error("Error during SNC discovery: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void processSubnetwork(MultiLayerSubnetwork_T subnetwork, CorbaConnection corbaConnection) throws ProcessingFailureException, SQLException {
        int HOW_MANY = ExecutionContext.getInstance().getCircle().getSncHowMuch();
        SubnetworkConnectionList_THolder subnetworkConnectionListTHolder = new SubnetworkConnectionList_THolder();
        SNCIterator_IHolder sncIteratorIHolder = new SNCIterator_IHolder();
        try {
            start = System.currentTimeMillis();
            corbaConnection.getMlsnManager().getAllSubnetworkConnections(ExecutionMode.DELTA == ExecutionContext.getInstance().getExecutionMode() ? buildDeltaSearchCriteria(subnetwork) : subnetwork.name, getDiscoveryItemType() == DiscoveryItemType.SNC_PACKET ? new short[]{309} : new short[]{}, HOW_MANY, subnetworkConnectionListTHolder, sncIteratorIHolder);
            SubnetworkConnection_T[] subnetworkConnectionTs = subnetworkConnectionListTHolder.value;
            log.info("Discovered SNCs: {}", subnetworkConnectionTs.length);
            if (isExecutionModeImport()) {
                saveSubnetworkConnections(subnetworkConnectionTs);
            } else {
                processDelta(subnetworkConnectionTs, corbaConnection);
            }

            discoveryCount = discoveryCount + subnetworkConnectionTs.length;
            subnetworkConnectionTs = null;
            processSubnetworkConnections(subnetworkConnectionListTHolder, sncIteratorIHolder, corbaConnection);
            end = System.currentTimeMillis();
            printDiscoveryResult(end - start);
        } catch (ProcessingFailureException e) {
            CorbaErrorHandler.handleProcessingFailureException(e, "processSubnetwork: " + Arrays.toString(subnetwork.name));
            log.error("Failed to process subnetwork: {}", Arrays.toString(subnetwork.name), e);
            throw e;
        }
    }

    private void processSubnetworkConnections(SubnetworkConnectionList_THolder sncListHolder, SNCIterator_IHolder iteratorHolder, CorbaConnection corbaConnection) throws ProcessingFailureException, SQLException {
        int batchSize = ExecutionContext.getInstance().getCircle().getTopologyHowMuch();
        SNCIterator_I iterator = iteratorHolder.value;
        if (iterator != null) {
            boolean hasMoreData = true;
            try {
                while (hasMoreData) {
                    hasMoreData = iterator.next_n(batchSize, sncListHolder);
                    discoveryCount = discoveryCount + sncListHolder.value.length;
                    SubnetworkConnection_T[] subnetworkConnectionTs = sncListHolder.value;
                    if (isExecutionModeImport()) {
                        saveSubnetworkConnections(subnetworkConnectionTs);
                    } else {
                        processDelta(subnetworkConnectionTs, corbaConnection);
                    }
                    subnetworkConnectionTs = null;
                    if (discoveryCount % 1000 == 0) {
                        log.info("Discovered SNCs so far: {}", discoveryCount);
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching SNCs : {}", e.getMessage(), e);
                throw e;
            }
        }

    }

    public void loadAll() {
        List<SNC> sncList = null;
        boolean isPacket = getDiscoveryItemType() == DiscoveryItemType.ROUTE_PACKET;
        if (isPacket) {
            sncList = sncRepository.findAllPacketSNCs(SNCResultSetMapper.getInstance()::mapToDto, true);
        } else {
            sncList = sncRepository.findAllSNCs(SNCResultSetMapper.getInstance()::mapToDto, true);
        }
        if (sncList != null && !sncList.isEmpty()) {
            SNCHolder.getInstance().setElements(CollectionUtils.convertListToMap(sncList, SNC::getSncId));
            if (isPacket) {
                log.info("Total Packet SNCs {} loaded from DB", sncList.size());
            } else {
                log.info("Total SNCs {} loaded from DB", sncList.size());
            }

        } else {
            log.error("No SNCs found to be loaded from the DB");
        }
        sncList = null;
    }

    private void logSubnetworkDetails(MultiLayerSubnetwork_T subnetwork) {
        Arrays.stream(subnetwork.name).forEach(attr ->
                log.debug("Subnetwork Attribute - Name: {}, Value: {}", attr.name, attr.value));
        Arrays.stream(subnetwork.additionalInfo).forEach(attr ->
                log.debug("Subnetwork Additional Info - Name: {}, Value: {}", attr.name, attr.value));
    }

    private void logSubnetworkDetails(SubnetworkConnection_T subnetwork) {
        Arrays.stream(subnetwork.name).forEach(attr ->
                log.debug("SNC Attribute - Name: {}, Value: {}", attr.name, attr.value));
        Arrays.stream(subnetwork.additionalInfo).forEach(attr ->
                log.debug("SNC Additional Info - Name: {}, Value: {}", attr.name, attr.value));
        for (TPData_T tpData : subnetwork.aEnd) {
            Arrays.stream(tpData.tpName).forEach(attr ->
                    log.debug("SNC aEnd Info - Name: {}, Value: {}", attr.name, attr.value));
        }

        for (TPData_T tpData : subnetwork.zEnd) {
            Arrays.stream(tpData.tpName).forEach(attr ->
                    log.debug("SNC zEnd Info - Name: {}, Value: {}", attr.name, attr.value));
        }


    }

    private List<MultiLayerSubnetwork_T> fetchAllSubnetworks(CorbaConnection corbaConnection) throws ProcessingFailureException {
        List<MultiLayerSubnetwork_T> subnetworks = new ArrayList<>();
        SubnetworkList_THolder subnetworkListHolder = new SubnetworkList_THolder();
        SubnetworkIterator_IHolder iteratorHolder = new SubnetworkIterator_IHolder();

        corbaConnection.getEmsManager().getAllTopLevelSubnetworks(5, subnetworkListHolder, iteratorHolder);
        Collections.addAll(subnetworks, subnetworkListHolder.value);

        if (iteratorHolder.value != null) {
            SubnetworkIterator_I iterator = iteratorHolder.value;
            while (iterator.next_n(10, subnetworkListHolder)) {
                Collections.addAll(subnetworks, subnetworkListHolder.value);
            }
        }
        return subnetworks;
    }

    public void processDelta(SubnetworkConnection_T[] subnetworkConnectionTs, CorbaConnection corbaConnection) {
        softDeleteSNCs(subnetworkConnectionTs);
        List<SubnetworkConnection_T> sncsToBeMerged = Arrays.stream(subnetworkConnectionTs)
                .filter(snc -> !SNCUtils.isSNCDeleted(snc.additionalInfo))
                .collect(Collectors.toList());
        if (!sncsToBeMerged.isEmpty()) {
            try {
                List<SNC> toBeMerged = SNCCorbaMapper.getInstance().mapFromCorbaList(sncsToBeMerged);
                sncRepository.upsertSNCs(toBeMerged);
                log.info("SNCs {} successfully merged into the database.", sncsToBeMerged.size());


                List<String> sncIdsToBeMerged = toBeMerged.stream().map(SNC::getSncId).collect(Collectors.toList());
                log.info("Merged SNCs will have routes deleted first..");
                RouteRepository routeRepository = RouteRepository.getInstance();
                routeRepository.deleteSNCRoutes(sncIdsToBeMerged);
                log.info("Merged SNCs routes deleted successfully....");
                // Run Get Route On them again ...

                log.info("Merged SNCs fresh getRoute to be called...");
                RouteService routeService = RouteService.getInstance(routeRepository);
                SNCHolder.getInstance().setElements(CollectionUtils.convertListToMap(toBeMerged, SNC::getSncId));
                routeService.discoverRoutes(corbaConnection);
                log.info("Merged SNCs fresh getRoute completed successfully...");
            } catch (SQLException e) {
                log.error("Failed to upsert topologies into the database.", e);
                throw new RuntimeException("Error upserting topologies.", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void runDeltaProcess(CorbaConnection corbaConnection) throws SQLException, ProcessingFailureException {
        discoverSubnetworkConnections(corbaConnection, ExecutionMode.DELTA);
    }

    private void softDeleteSNCs(SubnetworkConnection_T[] subnetworkConnectionTs) {
        List<String> sncsToBeDeleted = Arrays.stream(subnetworkConnectionTs).
                filter(snc -> SNCUtils.isSNCDeleted(snc.additionalInfo)).
                map(SNCUtils::getSNCId).collect(Collectors.toList());

        if (!sncsToBeDeleted.isEmpty()) {
            log.info("Found {} SNCs that were deleted from NMS, marking them deleted in the DB.", sncsToBeDeleted.size());
            log.info("Found {} SNCs that were deleted from NMS, deleting their routes ", sncsToBeDeleted.size());
            RouteRepository routeRepository = RouteRepository.getInstance();
            routeRepository.deleteSNCRoutes(sncsToBeDeleted);
            sncRepository.deleteSubnetworkConnections(sncsToBeDeleted, true);
        } else {
            log.info("No SNCs were found to be deleted from NMS, hence exiting.");
        }
    }

    private void softDeleteSNCs() {
        List<String> sncsToBeDeleted = subnetworkConnectionTList.
                stream().
                filter(snc -> SNCUtils.isSNCDeleted(snc.additionalInfo)).
                map(SNCUtils::getSNCId).collect(Collectors.toList());

        if (!sncsToBeDeleted.isEmpty()) {
            log.info("Found {} SNCs that were deleted from NMS, marking them deleted in the DB.", sncsToBeDeleted.size());
            sncRepository.deleteSubnetworkConnections(sncsToBeDeleted, true);
        } else {
            log.info("No SNCs were found to be deleted from NMS, hence exiting.");
        }
    }

    private void saveSubnetworkConnections() throws SQLException {
        long start = System.currentTimeMillis();
        sncRepository.insertSNCs(
                SNCCorbaMapper.getInstance().mapFromCorbaList(subnetworkConnectionTList),
                100
        );
        long end = System.currentTimeMillis();
        log.debug("Discovered SNCs # {} inserted in {} seconds.", subnetworkConnectionTList.size(), (end - start) / 1000);
    }

    private void saveSubnetworkConnections(SubnetworkConnection_T[] subnetworkConnections) throws SQLException {
        long start = System.currentTimeMillis();
        sncRepository.insertSNCs(
                SNCCorbaMapper.getInstance().mapFromCorbaArray(subnetworkConnections),
                100
        );
        long end = System.currentTimeMillis();
        log.debug("Discovered SNCs # {} inserted in {} seconds.", subnetworkConnections.length, (end - start) / 1000);
        subnetworkConnections = null;
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
        log.info("getDiscoveryItemType(): {}", getDiscoveryItemType());
        if (getDiscoveryItemType() == DiscoveryItemType.SNC_PACKET) {
            log.info("Delete  SNC_PACKET");
            return sncRepository.deleteSNCs(true);
        } else {
            log.info("Delete  SNC");
            return sncRepository.deleteSNCs(false);
        }
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
