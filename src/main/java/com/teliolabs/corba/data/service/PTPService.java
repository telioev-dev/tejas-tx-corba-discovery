package com.teliolabs.corba.data.service;

import com.teliolabs.corba.TxCorbaDiscoveryApplication;
import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.dto.PTP;
import com.teliolabs.corba.data.holder.ManagedElementHolder;
import com.teliolabs.corba.data.holder.PTPHolder;
import com.teliolabs.corba.data.mapper.PTPCorbaMapper;
import com.teliolabs.corba.data.mapper.PTPResultSetMapper;
import com.teliolabs.corba.data.repository.ManagedElementRepository;
import com.teliolabs.corba.data.repository.PTPRepository;
import com.teliolabs.corba.discovery.DiscoveryService;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.utils.CollectionUtils;
import com.teliolabs.corba.utils.CorbaConstants;
import com.teliolabs.corba.utils.ManagedElementUtils;
import com.teliolabs.corba.utils.PTPUtils;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;
import org.tmforum.mtnm.managedElement.ManagedElement_T;
import org.tmforum.mtnm.managedElementManager.ManagedElementMgr_I;
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
        boolean isDelta = ExecutionContext.getInstance().getExecutionMode() == ExecutionMode.DELTA;
        neNameArray = new NameAndStringValue_T[isDelta ? 3 : 2];
        neNameArray[0] = new NameAndStringValue_T(CorbaConstants.EMS_STR, ExecutionContext.getInstance().getCircle().getEms());
        neNameArray[1] = new NameAndStringValue_T();
        neNameArray[1].name = CorbaConstants.MANAGED_ELEMENT_STR;

        if (isDelta) {
            neNameArray[2] = new NameAndStringValue_T(CorbaConstants.TIMESTAMP_SIGNATURE_STR, TxCorbaDiscoveryApplication.DELTA_TIMESTAMP);
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

    private void softDeleteTerminationPoints() {
        List<TerminationPoint_T> terminationPointsToBeDeleted = terminationPoints.
                stream().
                filter(ptp -> PTPUtils.isTerminationPointDeleted(ptp.additionalInfo)).collect(Collectors.toList());

        if (!terminationPointsToBeDeleted.isEmpty()) {
            log.info("Found {} PTPs that were deleted from NMS, marking them deleted in the DB.", terminationPointsToBeDeleted.size());
            //ptpRepository.deleteTopologies(topologiesToBeDeleted);
        } else {
            log.info("No Topologies were found to be deleted from NMS, hence exiting.");
        }
    }

    public void discoverTerminationPoints(CorbaConnection corbaConnection) throws ProcessingFailureException, SQLException {
        meManager = corbaConnection.getMeManager();
        Map<String, ManagedElement> managedElements = ManagedElementHolder.getInstance().getElements();
        ExecutionMode executionMode = ExecutionContext.getInstance().getExecutionMode();
        if (managedElements != null && !managedElements.isEmpty()) {
            start = System.currentTimeMillis();
            Set<String> meNamesSet = managedElements.keySet();
            int i = 0;
            try {
                for (String meName : meNamesSet) {
                    if (ExecutionMode.DELTA == executionMode && meName.contains("UME"))
                        continue; // IGNORE UMEs for PTP

                    log.info("#{} ME '{}' for PTP processing.", i++, meName);
                    processManagedElementStack(meName);
                }
                end = System.currentTimeMillis();
                printDiscoveryResult(end - start);
                updateJobStatus();
            } catch (ProcessingFailureException e) {
                log.error("Error occurred during network calls for PTPs");
                e.printStackTrace();
                throw e;
            } catch (Exception e) {
                log.error("Error occurred during network calls for PTPs");
                e.printStackTrace();
                throw e;
            }
        } else {
            log.error("PTP discovery can't run as no MEs found in the DB");
            return;
        }

        log.debug("Total PTPs fetched from NMS: {}", discoveryCount);

        // TODO: Remove this later after testing.. no longer need after we switched to save then and there
//        if (terminationPoints != null && !terminationPoints.isEmpty() && ExecutionMode.IMPORT == executionMode) {
//            try {
//                saveTerminationPoints();
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        }
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
        log.info("Successfully saved {} Termination Points in {} seconds.", terminationPoints.size(), (end - start) / 1000);
    }

    public void discoverTerminationPointsSync(CorbaConnection corbaConnection, ExecutionMode executionMode) throws Exception {
        meManager = corbaConnection.getMeManager();
        ManagedElementRepository managedElementRepository = ManagedElementRepository.getInstance();
        managedElementService = ManagedElementService.getInstance(managedElementRepository);
        List<ManagedElement_T> managedElements = managedElementService.getManagedElements();
        if (managedElements == null || managedElements.isEmpty()) {
            log.info("ME discovery was not found to be run or failed, hence initiating again for PTPs");
            managedElements = ManagedElementService.getInstance().discoverManagedElements(corbaConnection, executionMode);
        } else {
            log.info("Discovered MEs found for PTP discovery, using them.");
        }

        if (managedElements != null && !managedElements.isEmpty()) {
            int size = managedElements.size();
            log.info("Total MEs for PTP processing:  {}", size);
            for (int i = 0; i < size; i++) { // TODO
                final ManagedElement_T managedElementT = managedElements.get(i);
                final String meName = ManagedElementUtils.getMEName(managedElementT.name);
                log.info("#{} ME '{}' for PTP processing.", i + 1, meName);
                try {
                    processManagedElementStack(meName);
                } catch (ProcessingFailureException e) {
                    log.error("Error occurred during network calls for PTPs");
                    e.printStackTrace();
                } catch (Exception e) {
                    log.error("Error occurred during network calls for PTPs");
                    e.printStackTrace();
                }
            }
        }

        log.debug("Total PTPs fetched from NMS: {}", terminationPoints.size());

        if (terminationPoints != null && !terminationPoints.isEmpty() && ExecutionMode.IMPORT == executionMode) {
            try {
                saveTerminationPoints();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void processManagedElementStack(String meName) throws ProcessingFailureException, SQLException {
        neNameArray[1].value = meName;
        List<TerminationPoint_T> terminationPoints = new ArrayList<>();
        int batchSize = ExecutionContext.getInstance().getCircle().getPtpHowMuch();
        TerminationPointList_THolder terminationPointListHolder = new TerminationPointList_THolder();
        TerminationPointIterator_IHolder terminationPointIteratorHolder = new TerminationPointIterator_IHolder();
        meManager.getAllPTPs(neNameArray, tpLayerRateList, connectionLayerRateList, batchSize, terminationPointListHolder, terminationPointIteratorHolder);
        Collections.addAll(terminationPoints, terminationPointListHolder.value);
        if (terminationPointIteratorHolder.value != null) {
            boolean exitWhile = false;
            try {
                boolean hasMoreData = true;
                while (hasMoreData) {
                    hasMoreData = terminationPointIteratorHolder.value.next_n(batchSize, terminationPointListHolder);
                    Collections.addAll(terminationPoints, terminationPointListHolder.value);
                }
                exitWhile = true;
            } finally {
                if (!exitWhile) {
                    terminationPointIteratorHolder.value.destroy();
                }
            }
            discoveryCount = discoveryCount + terminationPoints.size();
            log.info("Total Termination Points discovered so far: {}", discoveryCount);
        }
        saveTerminationPoints(terminationPoints);
    }

    private void processManagedElementSync(String meName) throws ProcessingFailureException {
        neNameArray[1].value = meName;
        int batchSize = ExecutionContext.getInstance().getCircle().getPtpHowMuch();
        meManager.getAllPTPs(neNameArray, tpLayerRateList, connectionLayerRateList, batchSize, terminationPointList, terminationPointIterator);
        log.debug("getAllPTPs: got {} PTP for ME {}.", terminationPointList.value.length, neNameArray[1].value);
        Collections.addAll(terminationPoints, terminationPointList.value);
        if (terminationPointIterator.value != null) {
            boolean exitWhile = false;
            try {
                boolean hasMoreData = true;
                while (hasMoreData) {
                    hasMoreData = terminationPointIterator.value.next_n(batchSize, terminationPointList);
                    Collections.addAll(terminationPoints, terminationPointList.value);
                }
                exitWhile = true;
            } finally {
                if (!exitWhile) {
                    terminationPointIterator.value.destroy();
                }
            }

        }
        log.debug("getAllPTPs: total PTPs so far {}.", terminationPoints.size());
        clearArrayAndIterator(terminationPointList, terminationPointIterator);
    }

    private void processManagedElement(ManagedElement_T managedElement) throws ProcessingFailureException {
        NameAndStringValue_T[] neNameArray = buildPTPSearchCriteria(managedElement);
        log.info("Thread: {} Processing ME '{}' for PTP discovery", Thread.currentThread().getName(), neNameArray[1].value);
        int batchSize = ExecutionContext.getInstance().getCircle().getPtpHowMuch();
        TerminationPointList_THolder terminationPointList = new TerminationPointList_THolder();
        TerminationPointIterator_IHolder terminationPointIterator = new TerminationPointIterator_IHolder();
        meManager.getAllPTPs(neNameArray, tpLayerRateList, connectionLayerRateList, batchSize, terminationPointList, terminationPointIterator);
        log.info("getAllPTPs: got {} PTP for ME {}.", terminationPointList.value.length, neNameArray[1].value);
        Collections.addAll(terminationPointsSync, terminationPointList.value);
        if (terminationPointIterator.value != null) {
            boolean exitWhile = false;
            try {
                boolean hasMoreData = true;
                while (hasMoreData) {
                    hasMoreData = terminationPointIterator.value.next_n(batchSize, terminationPointList);
                    Collections.addAll(terminationPointsSync, terminationPointList.value);
                }
                exitWhile = true;
            } finally {
                if (!exitWhile) {
                    terminationPointIterator.value.destroy();
                }
            }

        }
        log.info("getAllPTPs: total PTPs so far {}.", terminationPointsSync.size());
        clearArrayAndIterator(terminationPointList, terminationPointIterator);
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

