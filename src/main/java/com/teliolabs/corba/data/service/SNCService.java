package com.teliolabs.corba.data.service;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.dto.PTP;
import com.teliolabs.corba.data.dto.SNC;
import com.teliolabs.corba.data.holder.PTPHolder;
import com.teliolabs.corba.data.holder.SNCHolder;
import com.teliolabs.corba.data.mapper.PTPResultSetMapper;
import com.teliolabs.corba.data.mapper.SNCCorbaMapper;
import com.teliolabs.corba.data.mapper.SNCResultSetMapper;
import com.teliolabs.corba.data.mapper.TopologyCorbaMapper;
import com.teliolabs.corba.data.repository.SNCRepository;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.transport.CorbaSession;
import com.teliolabs.corba.utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.common.Common_IHolder;
import org.tmforum.mtnm.emsMgr.EMSMgr_I;
import org.tmforum.mtnm.emsMgr.EMSMgr_IHelper;
import org.tmforum.mtnm.emsSession.EmsSession_I;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;
import org.tmforum.mtnm.multiLayerSubnetwork.*;
import org.tmforum.mtnm.subnetworkConnection.*;
import org.tmforum.mtnm.terminationPoint.TerminationPoint_T;
import org.tmforum.mtnm.topologicalLink.TopologicalLinkIterator_I;
import org.tmforum.mtnm.topologicalLink.TopologicalLinkIterator_IHolder;
import org.tmforum.mtnm.topologicalLink.TopologicalLinkList_THolder;
import org.tmforum.mtnm.topologicalLink.TopologicalLink_T;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class SNCService {

    private static SNCService instance;
    private final SNCRepository sncRepository;

    private SubnetworkConnectionList_THolder sncList = new SubnetworkConnectionList_THolder();
    private SNCIterator_IHolder sncIterator = new SNCIterator_IHolder();
    private List<SubnetworkConnection_T> subnetworkConnectionTList = new ArrayList<>();
    private List<SNC> sncs = new ArrayList<>();
    private Integer discoveredSncCount = 0;


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
                subnetworkName = s.value;
            }
        }

        return new NameAndStringValue_T[]{
                new NameAndStringValue_T(CorbaConstants.EMS_STR, emsName),
                new NameAndStringValue_T(CorbaConstants.MULTILAYER_SUBNETWORK_STR, subnetworkName),
                new NameAndStringValue_T(CorbaConstants.TIMESTAMP_SIGNATURE_STR, DateTimeUtils.getDeltaTimestamp(1))
        };
    }

    public void discoverSubnetworkConnections(CorbaConnection corbaConnection, ExecutionMode executionMode) throws SQLException, ProcessingFailureException {

        try {
            List<MultiLayerSubnetwork_T> subnetworks = fetchAllSubnetworks(corbaConnection);
            for (MultiLayerSubnetwork_T subnetwork : subnetworks) {
                logSubnetworkDetails(subnetwork);
                processSubnetwork(subnetwork, corbaConnection);
            }

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
        short[] rateList = new short[0];
        try {
            long start = System.currentTimeMillis();
            corbaConnection.getMlsnManager().getAllSubnetworkConnections(ExecutionMode.DELTA == ExecutionContext.getInstance().getExecutionMode() ?
                    buildDeltaSearchCriteria(subnetwork) : subnetwork.name, rateList, HOW_MANY, sncList, sncIterator);
            Collections.addAll(subnetworkConnectionTList, sncList.value);
            processSubnetworkConnections(sncList, sncIterator);
            long end = System.currentTimeMillis();
            log.info("Network discovery on Subnetwork {} for total SNCs {} took {} seconds.", SNCUtils.getMultilayerSubnetworkName(subnetwork), subnetworkConnectionTList.size(), (end - start) / 1000);
        } catch (Exception e) {
            log.error("Failed to process subnetwork: {}", Arrays.toString(subnetwork.name), e);
            throw e;
        }
    }

    private void processSubnetworkConnections(SubnetworkConnectionList_THolder sncListHolder, SNCIterator_IHolder iteratorHolder) throws ProcessingFailureException, SQLException {
        int batchSize = ExecutionContext.getInstance().getCircle().getTopologyHowMuch();
        if (iteratorHolder.value != null) {
            SNCIterator_I iterator = iteratorHolder.value;
            boolean hasMoreData = true;
            try {
                while (hasMoreData) {
                    hasMoreData = iterator.next_n(batchSize, sncListHolder);
                    //Collections.addAll(subnetworkConnectionTList, sncListHolder.value);
                    discoveredSncCount = discoveredSncCount + sncListHolder.value.length;
                    List<SubnetworkConnection_T> tempList = Arrays.asList(sncListHolder.value);
                    saveSubnetworkConnections(tempList);
                    tempList = null;
                    //log.info("Discovered SNCs so far: {}", subnetworkConnectionTList.size());
                    log.info("Discovered SNCs so far: {}", discoveredSncCount);
                }
            } catch (ProcessingFailureException e) {
                log.error("Error fetching SNCs : {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                log.error("Error fetching SNCs : {}", e.getMessage(), e);
                throw e;
            }
        }

    }

    public void loadAll() {
        List<SNC> sncList = sncRepository.findAllSNCs(SNCResultSetMapper.getInstance()::mapToDto);
        SNCHolder.getInstance().setElements(CollectionUtils.convertListToMap(sncList, SNC::getSncId));
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


    public void runDeltaProcess(CorbaConnection corbaConnection) throws SQLException, ProcessingFailureException {
        discoverSubnetworkConnections(corbaConnection, ExecutionMode.DELTA);
        if (subnetworkConnectionTList == null || subnetworkConnectionTList.isEmpty()) {
            log.warn("No Delta SNCs discovered from NMS!");
            return;
        }

        log.info("SNCs discovered for delta processing: {}", subnetworkConnectionTList.size());

        softDeleteSNCs();

        List<SubnetworkConnection_T> sncsToBeMerged = subnetworkConnectionTList.stream()
                .filter(snc -> !SNCUtils.isSNCDeleted(snc.additionalInfo))
                .collect(Collectors.toList());

        log.info("Any SNCs that were not deleted?: {}", sncsToBeMerged.size());

        if (sncsToBeMerged.isEmpty()) {
            log.info("No SNCs to be merged after filtering deleted SNCs.");
            return;
        }

        try {
            sncRepository.upsertSNCs(
                    SNCCorbaMapper.getInstance().mapFromCorbaList(sncsToBeMerged)
            );
            log.info("SNCs successfully merged into the database.");
        } catch (SQLException e) {
            log.error("Failed to upsert topologies into the database.", e);
            throw new RuntimeException("Error upserting topologies.", e);
        }
    }

    private void softDeleteSNCs() {
        List<String> sncsToBeDeleted = subnetworkConnectionTList.
                stream().
                filter(snc -> SNCUtils.isSNCDeleted(snc.additionalInfo)).
                map(SNCUtils::getSNCId).collect(Collectors.toList());

        if (!sncsToBeDeleted.isEmpty()) {
            log.info("Found {} SNCs that were deleted from NMS, marking them deleted in the DB.", sncsToBeDeleted.size());
            sncRepository.deleteSubnetworkConnections(sncsToBeDeleted);
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
        log.info("Discovered SNCs # {} inserted in {} seconds.", subnetworkConnectionTList.size(), (end - start) / 1000);
    }

    private void saveSubnetworkConnections(List<SubnetworkConnection_T> subnetworkConnections) throws SQLException {
        long start = System.currentTimeMillis();
        sncRepository.insertSNCs(
                SNCCorbaMapper.getInstance().mapFromCorbaList(subnetworkConnections),
                50
        );
        long end = System.currentTimeMillis();
        log.info("Discovered SNCs # {} inserted in {} seconds.", subnetworkConnections.size(), (end - start) / 1000);
        subnetworkConnections = null;
    }
}
