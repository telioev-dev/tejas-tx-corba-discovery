package com.teliolabs.corba.data.service;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.dto.Topology;
import com.teliolabs.corba.data.mapper.TopologyCorbaMapper;
import com.teliolabs.corba.data.repository.TopologyRepository;
import com.teliolabs.corba.discovery.DiscoveryService;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.transport.CorbaErrorHandler;
import com.teliolabs.corba.utils.CorbaConstants;
import com.teliolabs.corba.utils.DateTimeUtils;
import com.teliolabs.corba.utils.TopologyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;
import org.tmforum.mtnm.multiLayerSubnetwork.MultiLayerSubnetwork_T;
import org.tmforum.mtnm.multiLayerSubnetwork.SubnetworkIterator_I;
import org.tmforum.mtnm.multiLayerSubnetwork.SubnetworkIterator_IHolder;
import org.tmforum.mtnm.multiLayerSubnetwork.SubnetworkList_THolder;
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
@RequiredArgsConstructor
public class TopologyService implements DiscoveryService {

    private static TopologyService instance;
    private final TopologyRepository topologyRepository;
    private Integer discoveryCount = 0;
    private long start;
    private long end;

    // Public method to get the singleton instance
    public static TopologyService getInstance(TopologyRepository topologyRepository) {
        if (instance == null) {
            synchronized (TopologyService.class) {
                if (instance == null) {
                    instance = new TopologyService(topologyRepository);
                    log.debug("TopologyService instance created.");
                }
            }
        }
        return instance;
    }

    private List<TopologicalLink_T> topologicalLinkTList = new ArrayList<>();

    private NameAndStringValue_T[] buildDeltaSearchCriteria(MultiLayerSubnetwork_T subnetwork) {
        if (subnetwork == null || subnetwork.name == null || subnetwork.name.length == 0) {
            log.warn("Subnetwork is null or has no name attributes");
            return new NameAndStringValue_T[0];
        }

        log.info("Building delta search criteria for topologies discovery.");
        String emsName = null;
        String subnetworkName = null;

        for (NameAndStringValue_T s : subnetwork.name) {
            if (CorbaConstants.EMS_STR.equalsIgnoreCase(s.name)) {
                emsName = s.value;
            } else if (CorbaConstants.MULTILAYER_SUBNETWORK_STR.equalsIgnoreCase(s.name)) {
                subnetworkName = s.value;
            }
        }

        return new NameAndStringValue_T[] {
                new NameAndStringValue_T(CorbaConstants.EMS_STR, emsName),
                new NameAndStringValue_T(CorbaConstants.MULTILAYER_SUBNETWORK_STR, subnetworkName),
                new NameAndStringValue_T(CorbaConstants.TIMESTAMP_SIGNATURE_STR,
                        ExecutionContext.getInstance().getDeltaTimestamp())
        };
    }

    public void processDelta(TopologicalLink_T[] topologicalLinkTs) {
        softDeleteTopologies(topologicalLinkTs);
        List<TopologicalLink_T> topologiesToBeMerged = Arrays.stream(topologicalLinkTs)
                .filter(topology -> !TopologyUtils.isTopologyDeleted(topology.additionalInfo))
                .collect(Collectors.toList());
        if (!topologiesToBeMerged.isEmpty()) {
            try {
                topologyRepository.upsertTopologies(
                        TopologyCorbaMapper.getInstance().mapFromCorbaList(topologiesToBeMerged));
                log.info("Topologies successfully merged into the database.");
            } catch (SQLException e) {
                log.error("Failed to upsert topologies into the database.", e);
                throw new RuntimeException("Error upserting topologies.", e);
            }
        }
    }

    public void runDeltaProcess(CorbaConnection corbaConnection) throws ProcessingFailureException {
        discoverTopologies(corbaConnection, ExecutionMode.DELTA);
        if (topologicalLinkTList == null || topologicalLinkTList.isEmpty()) {
            log.warn("No topologies discovered from NMS!");
            return;
        }

        log.info("Topologies discovered for delta processing: {}", topologicalLinkTList.size());

        softDeleteTopologies();

        List<TopologicalLink_T> topologiesToBeMerged = topologicalLinkTList.stream()
                .filter(topology -> !TopologyUtils.isTopologyDeleted(topology.additionalInfo))
                .collect(Collectors.toList());

        log.info("Any topologies that were not deleted?: {}", topologiesToBeMerged.size());

        if (topologiesToBeMerged.isEmpty()) {
            log.info("No topologies to be merged after filtering deleted topologies.");
            return;
        }

        try {
            topologyRepository.upsertTopologies(
                    TopologyCorbaMapper.getInstance().mapFromCorbaList(topologiesToBeMerged));
            log.info("Topologies successfully merged into the database.");
        } catch (SQLException e) {
            log.error("Failed to upsert topologies into the database.", e);
            throw new RuntimeException("Error upserting topologies.", e);
        }
    }

    private void softDeleteTopologies(TopologicalLink_T[] topologicalLinkTs) {
        List<String> topologiesToBeDeleted = Arrays.stream(topologicalLinkTs)
                .filter(topology -> TopologyUtils.isTopologyDeleted(topology.additionalInfo))
                .map(topology -> TopologyUtils.getLinkName(topology.name)).collect(Collectors.toList());

        if (!topologiesToBeDeleted.isEmpty()) {
            log.info("Found {} Topologies that were deleted from NMS, marking them deleted in the DB.",
                    topologiesToBeDeleted.size());
            log.info("These are topologies deleted: {}", topologiesToBeDeleted);
            topologyRepository.deleteTopologies(topologiesToBeDeleted, true);
        } else {
            log.info("No Topologies were found to be deleted from NMS, hence exiting.");
        }
    }

    private void softDeleteTopologies() {
        List<String> topologiesToBeDeleted = topologicalLinkTList.stream()
                .filter(topology -> TopologyUtils.isTopologyDeleted(topology.additionalInfo))
                .map(topology -> TopologyUtils.getLinkName(topology.name)).collect(Collectors.toList());

        if (!topologiesToBeDeleted.isEmpty()) {
            log.info("Found {} Topologies that were deleted from NMS, marking them deleted in the DB.",
                    topologiesToBeDeleted.size());
            log.info("These are topologies deleted: {}", topologiesToBeDeleted);
            topologyRepository.deleteTopologies(topologiesToBeDeleted, true);
        } else {
            log.info("No Topologies were found to be deleted from NMS, hence exiting.");
        }
    }

    public void discoverTopologies(CorbaConnection corbaConnection, ExecutionMode executionMode)
            throws ProcessingFailureException {

        try {
            List<MultiLayerSubnetwork_T> subnetworks = fetchAllSubnetworks(corbaConnection);

            for (MultiLayerSubnetwork_T subnetwork : subnetworks) {
                logSubnetworkDetails(subnetwork);
                processSubnetwork(subnetwork, corbaConnection);
            }
            updateJobStatus();
            if (topologicalLinkTList != null && !topologicalLinkTList.isEmpty()
                    && executionMode == ExecutionMode.IMPORT) {
                saveTopologies();
            }
        } catch (ProcessingFailureException | SQLException e) {
            if (e instanceof ProcessingFailureException) {
                log.info("Wasn instance");
                CorbaErrorHandler.handleProcessingFailureException((ProcessingFailureException) e,
                        "discoverTopologies");
            } else {
                log.info("Wasn't instance");
            }
            log.error("Error during topology discovery: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public List<Topology> findAllTopologies() {
        return topologyRepository.findAllTopologies();
    }

    private List<MultiLayerSubnetwork_T> fetchAllSubnetworks(CorbaConnection corbaConnection)
            throws ProcessingFailureException {
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

    // private void processSubnetwork(MultiLayerSubnetwork_T subnetwork,
    // CorbaConnection corbaConnection) throws SQLException,
    // ProcessingFailureException {
    // try {
    // TopologicalLinkList_THolder linkListHolder = new
    // TopologicalLinkList_THolder();
    // TopologicalLinkIterator_IHolder iteratorHolder = new
    // TopologicalLinkIterator_IHolder();

    // int batchSize =
    // ExecutionContext.getInstance().getCircle().getTopologyHowMuch();
    // start = System.currentTimeMillis();
    // corbaConnection.getEmsManager().getAllTopLevelTopologicalLinks(batchSize,
    // linkListHolder, iteratorHolder);

    // TopologicalLink_T[] topologicalLinkTs = linkListHolder.value;
    // if (isExecutionModeImport()) {
    // saveTopologies(topologicalLinkTs);
    // } else {
    // processDelta(topologicalLinkTs);
    // }
    // discoveryCount = discoveryCount + topologicalLinkTs.length;
    // topologicalLinkTs = null;
    // processTopologicalLinks(linkListHolder, iteratorHolder);
    // end = System.currentTimeMillis();
    // printDiscoveryResult(end - start);
    // } catch (Exception e) {
    // log.error("Failed to process subnetwork: {}",
    // Arrays.toString(subnetwork.name), e);
    // throw e;
    // }
    // }
    private void processSubnetwork(MultiLayerSubnetwork_T subnetwork, CorbaConnection corbaConnection)
            throws SQLException, ProcessingFailureException {
        try {
            TopologicalLinkList_THolder linkListHolder = new TopologicalLinkList_THolder();
            TopologicalLinkIterator_IHolder iteratorHolder = new TopologicalLinkIterator_IHolder();

            int batchSize = ExecutionContext.getInstance().getCircle().getTopologyHowMuch();
            start = System.currentTimeMillis();
            corbaConnection.getEmsManager().getAllTopLevelTopologicalLinks(batchSize, linkListHolder, iteratorHolder);
            // corbaConnection.getMlsnManager().getAllTopologicalLinks(ExecutionMode.DELTA
            // == ExecutionContext.getInstance().getExecutionMode() ?
            // buildDeltaSearchCriteria(subnetwork) : subnetwork.name, batchSize,
            // linkListHolder, iteratorHolder);
            TopologicalLink_T[] topologicalLinkTs = linkListHolder.value;
            ExecutionMode executionMode = ExecutionContext.getInstance().getExecutionMode();

            if (ExecutionMode.IMPORT.equals(executionMode)) {
                saveTopologies(topologicalLinkTs);
            } else if (ExecutionMode.DELTA.equals(executionMode)) {
                processDelta(topologicalLinkTs);
            }

            discoveryCount += topologicalLinkTs.length;
            topologicalLinkTs = null;
            processTopologicalLinks(linkListHolder, iteratorHolder);
            end = System.currentTimeMillis();
            printDiscoveryResult(end - start);
        } catch (Exception e) {
            log.error("Failed to process subnetwork: {}", Arrays.toString(subnetwork.name), e);
            throw e;
        }
    }

    private void processTopologicalLinks(TopologicalLinkList_THolder linkListHolder,
            TopologicalLinkIterator_IHolder iteratorHolder) throws ProcessingFailureException, SQLException {
        int batchSize = ExecutionContext.getInstance().getCircle().getTopologyHowMuch();
        if (iteratorHolder.value != null) {
            TopologicalLinkIterator_I iterator = iteratorHolder.value;
            boolean hasMoreData = true;
            try {
                while (hasMoreData) {
                    hasMoreData = iterator.next_n(batchSize, linkListHolder);
                    discoveryCount = discoveryCount + linkListHolder.value.length;
                    TopologicalLink_T[] topologicalLinkTs = linkListHolder.value;
                    ExecutionMode executionMode = ExecutionContext.getInstance().getExecutionMode();
                    if (ExecutionMode.IMPORT.equals(executionMode)) {
                        saveTopologies(topologicalLinkTs);
                    } else if (ExecutionMode.DELTA.equals(executionMode)) {
                        processDelta(topologicalLinkTs);
                    }
                    topologicalLinkTs = null;
                    linkListHolder.value = null;
                    if (discoveryCount % 1000 == 0) {
                        log.info("Topologies discovered so far: {}", discoveryCount);
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching topological links: {}", e.getMessage(), e);
                throw e;
            }
        }
    }

    private void saveTopologies() throws SQLException {
        long start = System.currentTimeMillis();
        topologyRepository.insertTopologies(
                TopologyCorbaMapper.getInstance().mapFromCorbaList(topologicalLinkTList),
                100);
        long end = System.currentTimeMillis();
        log.debug("Discovered Topologies # {} inserted in {} seconds.", topologicalLinkTList.size(),
                (end - start) / 1000);
    }

    private void saveTopologies(TopologicalLink_T[] topologicalLinkTs) throws SQLException {
        long start = System.currentTimeMillis();
        topologyRepository.insertTopologies(
                TopologyCorbaMapper.getInstance().mapFromCorbaArray(topologicalLinkTs),
                100);
        long end = System.currentTimeMillis();
        log.debug("Discovered Topologies # {} inserted in {} seconds.", topologicalLinkTs.length, (end - start) / 1000);
        topologicalLinkTs = null;
    }

    private void logSubnetworkDetails(MultiLayerSubnetwork_T subnetwork) {
        Arrays.stream(subnetwork.name)
                .forEach(attr -> log.debug("Subnetwork Attribute - Name: {}, Value: {}", attr.name, attr.value));
        Arrays.stream(subnetwork.additionalInfo)
                .forEach(attr -> log.debug("Subnetwork Additional Info - Name: {}, Value: {}", attr.name, attr.value));
    }

    private void logTopologyDetails(TopologicalLink_T subnetwork) {
        Arrays.stream(subnetwork.name)
                .forEach(attr -> log.debug("Topology Attribute - Name: {}, Value: {}", attr.name, attr.value));
        Arrays.stream(subnetwork.additionalInfo)
                .forEach(attr -> log.debug("Topology Additional Info - Name: {}, Value: {}", attr.name, attr.value));
        Arrays.stream(subnetwork.aEndTP)
                .forEach(attr -> log.debug("Topology aEndTP - Name: {}, Value: {}", attr.name, attr.value));
        Arrays.stream(subnetwork.zEndTP)
                .forEach(attr -> log.debug("Topology zEndTP - Name: {}, Value: {}", attr.name, attr.value));
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
        return topologyRepository.truncate();
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
        return DiscoveryItemType.TOPOLOGY;
    }
}
