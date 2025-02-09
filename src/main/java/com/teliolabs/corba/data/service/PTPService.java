package com.teliolabs.corba.data.service;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.dto.PTP;
import com.teliolabs.corba.data.holder.ManagedElementHolder;
import com.teliolabs.corba.data.holder.PTPHolder;
import com.teliolabs.corba.data.mapper.PTPCorbaMapper;
import com.teliolabs.corba.data.mapper.PTPResultSetMapper;
import com.teliolabs.corba.data.repository.PTPRepository;
import com.teliolabs.corba.data.types.CommunicationState;
import com.teliolabs.corba.discovery.DiscoveryService;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.transport.CorbaErrorHandler;
import com.teliolabs.corba.utils.CollectionUtils;
import com.teliolabs.corba.utils.CorbaConstants;
import com.teliolabs.corba.utils.PTPUtils;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;
import org.tmforum.mtnm.managedElement.ManagedElement_T;
import org.tmforum.mtnm.managedElementManager.ManagedElementMgr_I;
import org.tmforum.mtnm.terminationPoint.TerminationPointIterator_I;
import org.tmforum.mtnm.terminationPoint.TerminationPointIterator_IHolder;
import org.tmforum.mtnm.terminationPoint.TerminationPointList_THolder;
import org.tmforum.mtnm.terminationPoint.TerminationPoint_T;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class PTPService implements DiscoveryService {

    private static PTPService instance;
    private final PTPRepository ptpRepository;
    private List<TerminationPoint_T> terminationPoints = new ArrayList<TerminationPoint_T>();
    private List<TerminationPoint_T> terminationPointsSync = Collections.synchronizedList(new ArrayList<TerminationPoint_T>());
    private ManagedElementService managedElementService;
    private ManagedElementMgr_I meManager;
    private TerminationPointList_THolder terminationPointList = new TerminationPointList_THolder();
    private TerminationPointIterator_IHolder terminationPointIterator = new TerminationPointIterator_IHolder();
    private short[] tpLayerRateList = new short[0];
    private short[] connectionLayerRateList = new short[0];
    private NameAndStringValue_T[] neNameArray;
    private List<String> deltaFailedManagedElements = null;
    private Integer discoveryCount = 0;
    private long start;
    private long end;

    // Public method to get the singleton instance
    public static PTPService getInstance(PTPRepository ptpRepository) {
        if (instance == null) {
            synchronized (PTPService.class) {
                if (instance == null) {
                    instance = new PTPService(ptpRepository);
                    log.debug("PTPService instance created.");
                }
            }
        }
        return instance;
    }

    private PTPService(PTPRepository ptpRepository) {
        this.ptpRepository = ptpRepository;
        DiscoveryItemType discoveryItemType = ExecutionContext.getInstance().getEntity();
        if (DiscoveryItemType.PTP == discoveryItemType) {
            boolean isDelta = ExecutionContext.getInstance().getExecutionMode() == ExecutionMode.DELTA;
            neNameArray = new NameAndStringValue_T[isDelta ? 3 : 2];
            neNameArray[0] = new NameAndStringValue_T(CorbaConstants.EMS_STR, ExecutionContext.getInstance().getCircle().getEms());
            neNameArray[1] = new NameAndStringValue_T();
            neNameArray[1].name = CorbaConstants.MANAGED_ELEMENT_STR;
            if (isDelta) {
                deltaFailedManagedElements = new ArrayList<>();
                neNameArray[2] = new NameAndStringValue_T(CorbaConstants.TIMESTAMP_SIGNATURE_STR, ExecutionContext.getInstance().getDeltaTimestamp());
            }
        }
    }


    private NameAndStringValue_T[] buildPTPSearchCriteria(ManagedElement_T managedElement) {
        if (managedElement == null || managedElement.name.length == 0) {
            log.warn("Subnetwork is null or has no name attributes");
            return new NameAndStringValue_T[0];
        }

        log.info("Building search criteria for PTP discovery.");
        String emsName = null;
        String meName = null;

        for (NameAndStringValue_T s : managedElement.name) {
            if (CorbaConstants.EMS_STR.equalsIgnoreCase(s.name)) {
                emsName = s.value;
            } else if (CorbaConstants.MANAGED_ELEMENT_STR.equalsIgnoreCase(s.name)) {
                meName = s.value;
            }
        }

        return new NameAndStringValue_T[]{new NameAndStringValue_T(CorbaConstants.EMS_STR, emsName), new NameAndStringValue_T(CorbaConstants.MANAGED_ELEMENT_STR, meName)};
    }


    public void runDeltaProcess(CorbaConnection corbaConnection) throws SQLException, ProcessingFailureException {
        discoverTerminationPoints(corbaConnection);
        //discoverTerminationPointsSync(corbaConnection, ExecutionMode.DELTA);
        if (terminationPoints == null || terminationPoints.isEmpty()) {
            log.debug("No delta PTPs found, returning.");
            return;
        }

        softDeleteTerminationPoints();

        List<TerminationPoint_T> terminationPointsToBeUpSerted = terminationPoints.
                stream().
                filter(ptp -> !PTPUtils.isTerminationPointDeleted(ptp.additionalInfo)).collect(Collectors.toList());

        log.info("PTPs that need to be added/updated: {}", terminationPointsToBeUpSerted.size());

        loadAll();
    }

    private void processDelta(List<TerminationPoint_T> terminationPointTs) throws SQLException {
        if (terminationPointTs == null || terminationPointTs.isEmpty()) {
            log.debug("No delta PTPs found, returning.");
            return;
        }

        softDeleteTerminationPoints(terminationPointTs);

        List<TerminationPoint_T> newUpdatedTerminationPoints = terminationPointTs.
                stream().
                filter(ptp -> !PTPUtils.isTerminationPointDeleted(ptp.additionalInfo)).collect(Collectors.toList());
        if (!newUpdatedTerminationPoints.isEmpty()) {
            log.info("PTPss considered for upsert: {}", newUpdatedTerminationPoints.size());
            ptpRepository.upsertTerminationPoints(PTPCorbaMapper.getInstance().mapFromCorbaList(newUpdatedTerminationPoints), 50);
        }
        terminationPointTs = null;
    }

    public void loadAll() {
        List<PTP> ptpList = ptpRepository.findAllTerminationPoints(PTPResultSetMapper.getInstance()::mapToDto);
        if (ptpList == null || ptpList.isEmpty()) {
            log.error("No PTPs found to be loaded from the DB");
        } else {
            PTPHolder.getInstance().setElements(CollectionUtils.convertListToMap(ptpList, (ptp -> ptp.getMeName() + "_" + ptp.getPtpId())));
            log.info("Total PTPs {} loaded from DB", ptpList.size());
        }
        ptpList = null;
    }

    private void softDeleteTerminationPoints(List<TerminationPoint_T> terminationPoints) {
        List<TerminationPoint_T> terminationPointsToBeDeleted = terminationPoints.
                stream().
                filter(ptp -> PTPUtils.isTerminationPointDeleted(ptp.additionalInfo)).collect(Collectors.toList());

        if (!terminationPointsToBeDeleted.isEmpty()) {
            log.info("Found {} PTPs that were deleted from NMS, marking them deleted in the DB.", terminationPointsToBeDeleted.size());
            ptpRepository.deleteTerminationPoints(PTPCorbaMapper.getInstance().mapFromCorbaList(terminationPointsToBeDeleted), true);
        } else {
            log.info("No PTPs were found to be deleted from NMS, hence exiting.");
        }
    }

    private void softDeleteTerminationPoints() {
        List<TerminationPoint_T> terminationPointsToBeDeleted = terminationPoints.
                stream().
                filter(ptp -> PTPUtils.isTerminationPointDeleted(ptp.additionalInfo)).collect(Collectors.toList());

        if (!terminationPointsToBeDeleted.isEmpty()) {
            log.info("Found {} PTPs that were deleted from NMS, marking them deleted in the DB.", terminationPointsToBeDeleted.size());
            ptpRepository.deleteTerminationPoints(PTPCorbaMapper.getInstance().mapFromCorbaList(terminationPointsToBeDeleted), true);
        } else {
            log.info("No Topologies were found to be deleted from NMS, hence exiting.");
        }
    }

    public void discoverTerminationPoints(CorbaConnection corbaConnection) throws ProcessingFailureException, SQLException {
        meManager = corbaConnection.getMeManager();
        Map<String, ManagedElement> managedElements = ManagedElementHolder.getInstance().getElements();
        if (managedElements != null && !managedElements.isEmpty()) {
            start = System.currentTimeMillis();
            Set<String> meNamesSet = managedElements.keySet();
            int i = 0;
            for (String meName : meNamesSet) {
                ManagedElement managedElement = managedElements.get(meName);
                if ((isExecutionModeDelta() && meName.contains("UME")))
                    continue; // IGNORE UMEs for PTP

                log.info("#{} ME '{}' for PTP processing.", i++, meName);
                try {
                    processManagedElementStack(meName, isExecutionModeDelta());
                } catch (ProcessingFailureException e) {
                    CorbaErrorHandler.handleProcessingFailureException(e, "getAllPTP, ManagedElement: " + meName);
                    if (isExecutionModeDelta()) {
                        deltaFailedManagedElements.add(meName);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            if (isExecutionModeDelta()) {
                if (!deltaFailedManagedElements.isEmpty()) {
                    log.info("Found some failed MEs #{} that failed delta call for getAllEquipment, deleting their PTPs now", deltaFailedManagedElements.size());
                    log.info("Found some failed MEs #{} that failed delta call for getAllEquipment, running full discovery on 'em now.", deltaFailedManagedElements.size());
                    ptpRepository.deleteManagedElementPTPs(deltaFailedManagedElements);
                    for (String meName : deltaFailedManagedElements) {
                        processManagedElementStack(meName, false);
                    }
                }
            }
            end = System.currentTimeMillis();
            printDiscoveryResult(end - start);
            updateJobStatus();
        } else {
            log.error("PTP discovery can't run as no MEs found in the DB");
            return;
        }

        log.debug("Total PTPs fetched from NMS: {}", discoveryCount);
    }

    private void saveTerminationPoints() throws SQLException {
        log.info("Converting Corba PTPs to DTOs");
        List<PTP> ptpList = PTPCorbaMapper.getInstance().mapFromCorbaList(terminationPoints);

        long start = System.currentTimeMillis();
        ptpRepository.insertTerminationPoints(ptpList, 100);
        long end = System.currentTimeMillis();
        log.info("Successfully saved {} Termination Points in {} seconds.", terminationPoints.size(), (end - start) / 1000);
        saveInMemory(ptpList);
    }

    private void saveTerminationPoints(List<TerminationPoint_T> terminationPoints) throws SQLException {
        List<PTP> ptpList = PTPCorbaMapper.getInstance().mapFromCorbaList(terminationPoints);
        long start = System.currentTimeMillis();
        ptpRepository.insertTerminationPoints(ptpList, 50);
        long end = System.currentTimeMillis();
        log.debug("Successfully saved {} Termination Points in {} seconds.", terminationPoints.size(), (end - start) / 1000);
        terminationPoints = null;
    }

    private void processManagedElementStack(String meName, boolean invokeDelta) throws ProcessingFailureException, SQLException {
        neNameArray[1].value = meName;
        int batchSize = ExecutionContext.getInstance().getCircle().getPtpHowMuch();
        List<TerminationPoint_T> terminationPointTs = new ArrayList<>();
        TerminationPointList_THolder terminationPointListHolder = new TerminationPointList_THolder();
        TerminationPointIterator_IHolder terminationPointIteratorHolder = new TerminationPointIterator_IHolder();
        if (invokeDelta) {
            meManager.getAllPTPs(neNameArray, tpLayerRateList, connectionLayerRateList, batchSize, terminationPointListHolder, terminationPointIteratorHolder);
        } else {
            NameAndStringValue_T[] nameAndStringValueTs = new NameAndStringValue_T[2];
            nameAndStringValueTs[0] = new NameAndStringValue_T(CorbaConstants.EMS_STR, ExecutionContext.getInstance().getCircle().getEms());
            nameAndStringValueTs[1] = new NameAndStringValue_T(CorbaConstants.MANAGED_ELEMENT_STR, meName);
            meManager.getAllPTPs(nameAndStringValueTs, tpLayerRateList, connectionLayerRateList, batchSize, terminationPointListHolder, terminationPointIteratorHolder);
        }
        Collections.addAll(terminationPointTs, terminationPointListHolder.value);
        TerminationPointIterator_I terminationPointIterator = terminationPointIteratorHolder.value;
        if (terminationPointIterator != null) {
            boolean exitWhile = false;
            try {
                boolean hasMoreData = true;
                while (hasMoreData) {
                    hasMoreData = terminationPointIterator.next_n(batchSize, terminationPointListHolder);
                    Collections.addAll(terminationPointTs, terminationPointListHolder.value);
                    terminationPointListHolder.value = null;
                }
                exitWhile = true;
            } finally {
                if (!exitWhile) {
                    terminationPointIterator.destroy();
                }
            }
            if (discoveryCount % 1000 == 0) {
                log.info("Total Termination Points discovered so far: {}", discoveryCount);
            }
        }
        discoveryCount = discoveryCount + terminationPointTs.size();
        if (isExecutionModeImport()) {
            saveTerminationPoints(terminationPointTs);
        } else {
            terminationPointTs.forEach(this::logTerminationPointDetails);
            processDelta(terminationPointTs);
        }
        neNameArray[1].value = null;
    }

    private void logTerminationPointDetails(TerminationPoint_T terminationPoint) {
        Arrays.stream(terminationPoint.name).forEach(attr ->
                log.info("PTP Attribute - Name: {}, Value: {}", attr.name, attr.value));
        Arrays.stream(terminationPoint.additionalInfo).forEach(attr ->
                log.info("PTP Additional Info - Name: {}, Value: {}", attr.name, attr.value));
    }

    private void logManagedElementDetails(ManagedElement_T managedElement) {
        Arrays.stream(managedElement.name).forEach(attr ->
                log.info("ME Attribute - Name: {}, Value: {}", attr.name, attr.value));
        Arrays.stream(managedElement.additionalInfo).forEach(attr ->
                log.info("ME Additional Info - Name: {}, Value: {}", attr.name, attr.value));
    }

    private void clearArrayAndIterator(TerminationPointList_THolder terminationPointList, TerminationPointIterator_IHolder terminationPointIterator) {
        clearArrayAndElements(terminationPointList);  // Clear the array and its elements
        clearIteratorReference(terminationPointIterator);  // Clear the iterator reference
    }

    private void clearIteratorReference(TerminationPointIterator_IHolder terminationPointIterator) {
        if (terminationPointIterator != null) {
            terminationPointIterator.value = null;  // Nullify the iterator reference
        }
    }

    private void clearArrayAndElements(TerminationPointList_THolder terminationPointList) {
        if (terminationPointList != null) {
            if (terminationPointList.value != null) {
                for (int i = 0; i < terminationPointList.value.length; i++) {
                    terminationPointList.value[i] = null;  // Nullify each element
                }
            }
            terminationPointList.value = null;  // Nullify the array reference
        }
    }

    private void saveInMemory(List<PTP> ptpList) {
        log.debug("Saving PTPs in memory: {}", (ptpList != null && !ptpList.isEmpty()));
        PTPHolder.getInstance().setElements(PTPCorbaMapper.getInstance().toMap(ptpList));
        ptpList = null;
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
        return 0;
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
        return DiscoveryItemType.PTP;
    }
}

