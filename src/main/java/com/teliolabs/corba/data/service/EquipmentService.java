package com.teliolabs.corba.data.service;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.domain.EquipmentEntity;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.holder.EquipmentHolder;
import com.teliolabs.corba.data.holder.ManagedElementHolder;
import com.teliolabs.corba.data.mapper.EquipmentCorbaDtoMapper;
import com.teliolabs.corba.data.mapper.EquipmentCorbaEntityMapper;
import com.teliolabs.corba.data.repository.EquipmentRepository;
import com.teliolabs.corba.data.types.CommunicationState;
import com.teliolabs.corba.discovery.DiscoveryService;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.transport.CorbaErrorHandler;
import com.teliolabs.corba.utils.CorbaConstants;
import com.teliolabs.corba.utils.EquipmentUtils;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.equipment.*;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
public class EquipmentService implements DiscoveryService {

    private static EquipmentService instance;
    private final EquipmentRepository equipmentRepository;
    private NameAndStringValue_T[] neNameArray;
    private EquipmentInventoryMgr_I eiManager;
    private List<EquipmentOrHolder_T> equipmentOrHolderTList = new ArrayList<>();
    private Integer discoveryCount = 0;
    private List<String> deltaFailedManagedElements = null;
    private long start;
    private long end;


    // Public method to get the singleton instance
    public static EquipmentService getInstance(EquipmentRepository equipmentRepository, CorbaConnection corbaConnection) {
        if (instance == null) {
            synchronized (EquipmentService.class) {
                if (instance == null) {
                    instance = new EquipmentService(equipmentRepository);
                    instance.eiManager = corbaConnection.getEiManager();
                    log.debug("EquipmentService instance created.");
                }
            }
        }
        return instance;
    }


    private EquipmentService(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
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

    public void runDeltaProcess() throws SQLException, ProcessingFailureException {
        discoverEquipments();
    }


    private void processDelta(List<EquipmentOrHolder_T> equipmentOrHolderTList) throws SQLException {
        softDeleteEquipments(equipmentOrHolderTList);

        List<EquipmentOrHolder_T> newUpdatedEquipments = equipmentOrHolderTList.stream().
                filter(equipmentOrHolderT -> equipmentOrHolderT.discriminator().value() == 0).
                filter(equip -> !EquipmentUtils.isEquipmentDeleted(equip.equip().additionalInfo)).collect(Collectors.toList());
        if (newUpdatedEquipments != null && !newUpdatedEquipments.isEmpty()) {
            log.info("EQs considered for upsert: {}", newUpdatedEquipments.size());
            equipmentRepository.upsertEquipments(EquipmentCorbaDtoMapper.getInstance().mapFromCorbaList(newUpdatedEquipments), 50);
        }
        equipmentOrHolderTList = null;
    }

    private void softDeleteEquipments(List<EquipmentOrHolder_T> equipmentOrHolderTList) {
        List<EquipmentOrHolder_T> collect = equipmentOrHolderTList.stream().
                filter(equipmentOrHolderT -> equipmentOrHolderT.discriminator().value() == 0).
                filter(equip -> EquipmentUtils.isEquipmentDeleted(equip.equip().additionalInfo)).collect(Collectors.toList());

        if (!collect.isEmpty()) {
            log.info("Found {} EQs that were deleted from NMS, marking them deleted in the DB.", collect.size());
            equipmentRepository.deleteEquipments(EquipmentCorbaDtoMapper.getInstance().mapFromCorbaList(collect), true);
        } else {
            log.info("No EQs were found to be deleted from NMS, hence exiting.");
        }
    }

    public void discoverEquipments() throws ProcessingFailureException, SQLException {
        Map<String, ManagedElement> managedElements = ManagedElementHolder.getInstance().getElements();
        if (managedElements != null && !managedElements.isEmpty()) {
            start = System.currentTimeMillis();
            Set<String> meNamesSet = managedElements.keySet();
            int i = 0;

            for (String meName : meNamesSet) {
                ManagedElement managedElement = managedElements.get(meName);
                if ((isExecutionModeDelta() && meName.contains("UME")) || CommunicationState.UNAVAILABLE == managedElement.getCommunicationState())
                    continue; // IGNORE UMEs for Equipment


                log.info("#{} ME '{}' for Equipment processing.", i++, meName);
                try {
                    processManagedElement(meName, isExecutionModeDelta());
                } catch (ProcessingFailureException e) {
                    log.info("Error occurred...");
                    CorbaErrorHandler.handleProcessingFailureException(e, "getAllEquipment, ManagedElement: " + meName);
                    if (e.errorReason.equals("EventLost") || e.errorReason.contains("ENTITY_NOT_FOUND")) {
                        log.error("getAllEquipment: '{}' failed with reason: {}, invoking without delta", meName, e.errorReason);
                        deltaFailedManagedElements.add(meName);
                    } else {
                        deltaFailedManagedElements.add(meName);
                    }
                } catch (SQLException e) {
                    throw e;
                }
            }

            if (isExecutionModeDelta() && (deltaFailedManagedElements != null && !deltaFailedManagedElements.isEmpty())) {
                log.info("Found some failed MEs #{} that failed delta call for getAllEquipment, running full discovery on 'em now.", deltaFailedManagedElements.size());
                // Run fullDiscovery on such MEs
                equipmentRepository.deleteManagedElementEquipments(deltaFailedManagedElements);
                for (String meName : deltaFailedManagedElements) {
                    processManagedElement(meName, false);
                }
            }
            end = System.currentTimeMillis();
            printDiscoveryResult(end - start);
            updateJobStatus();
        } else {
            log.error("Equipment discovery can't run as no MEs found in the DB");
            return;
        }
        log.debug("Total Equipments fetched from NMS: {}", discoveryCount);
    }

    private void saveEquipments() throws SQLException {
        log.info("Converting Corba Equipments to DTOs");
        List<EquipmentEntity> equipmentList = EquipmentCorbaEntityMapper.getInstance().mapFromCorbaList(equipmentOrHolderTList);

        long start = System.currentTimeMillis();
        equipmentRepository.insertEquipments(equipmentList, 100);
        long end = System.currentTimeMillis();
        log.info("Successfully saved {} Termination Points in {} seconds.", equipmentOrHolderTList.size(), (end - start) / 1000);
        saveInMemory(equipmentList);
    }

    private void saveEquipments(List<EquipmentOrHolder_T> equipmentOrHolderTList) throws SQLException {
        List<EquipmentEntity> equipmentList = EquipmentCorbaEntityMapper.getInstance().mapFromCorbaList(equipmentOrHolderTList);
        long start = System.currentTimeMillis();
        equipmentRepository.insertEquipments(equipmentList, 50);
        long end = System.currentTimeMillis();
        log.debug("Successfully saved {} Equipments in {} seconds.", equipmentOrHolderTList.size(), (end - start) / 1000);
    }

    private void saveInMemory(List<EquipmentEntity> equipmentList) {
        log.debug("Saving Equipments in memory: {}", (equipmentList != null && !equipmentList.isEmpty()));
        EquipmentHolder.getInstance().setElements(EquipmentCorbaEntityMapper.getInstance().toMap(equipmentList));
        equipmentList = null;
    }

    private void processManagedElement(String meName, boolean invokeDelta) throws ProcessingFailureException, SQLException {
        neNameArray[1].value = meName;
        ExecutionMode executionMode = ExecutionContext.getInstance().getExecutionMode();
        List<EquipmentOrHolder_T> equipmentOrHolderTList = new ArrayList<>();
        int HOW_MANY = ExecutionContext.getInstance().getCircle().getPtpHowMuch();
        EquipmentOrHolderList_THolder equipOrHolderList = new EquipmentOrHolderList_THolder();
        EquipmentOrHolderIterator_IHolder equipOrHolderItr = new EquipmentOrHolderIterator_IHolder();
        if (invokeDelta) {
            eiManager.getAllEquipment(neNameArray, HOW_MANY, equipOrHolderList, equipOrHolderItr);
        } else {
            NameAndStringValue_T[] nameAndStringValueTs = new NameAndStringValue_T[2];
            nameAndStringValueTs[0] = new NameAndStringValue_T(CorbaConstants.EMS_STR, ExecutionContext.getInstance().getCircle().getEms());
            nameAndStringValueTs[1] = new NameAndStringValue_T(CorbaConstants.MANAGED_ELEMENT_STR, meName);
            eiManager.getAllEquipment(nameAndStringValueTs, HOW_MANY, equipOrHolderList, equipOrHolderItr);
        }

        Collections.addAll(equipmentOrHolderTList, equipOrHolderList.value);
        if (equipOrHolderItr.value != null) {
            boolean exitWhile = false;
            try {
                boolean hasMoreData = true;
                while (hasMoreData) {
                    hasMoreData = equipOrHolderItr.value.next_n(HOW_MANY, equipOrHolderList);
                    Collections.addAll(equipmentOrHolderTList, equipOrHolderList.value);
                }
                exitWhile = true;
            } finally {
                if (!exitWhile) {
                    equipOrHolderItr.value.destroy();
                }
            }
        }
        discoveryCount = discoveryCount + equipmentOrHolderTList.size();
        log.info("getAllEquipment: total EQs discovered so far {}.", discoveryCount);
        if (executionMode == ExecutionMode.IMPORT) {
            saveEquipments(equipmentOrHolderTList);
        } else {
            equipmentOrHolderTList.forEach(this::logEquipmentDetails);
            processDelta(equipmentOrHolderTList);
        }

    }

    private void logEquipmentDetails(EquipmentOrHolder_T equipmentOrHolderT) {
        if (equipmentOrHolderT.discriminator().value() == 0) {
            Equipment_T equipment = equipmentOrHolderT.equip();
            log.info("EQ UserLabel: {}", equipment.userLabel);
            Arrays.stream(equipment.name).forEach(attr ->
                    log.info("EQ Attribute - Name: {}, Value: {}", attr.name, attr.value));
            Arrays.stream(equipment.additionalInfo).forEach(attr ->
                    log.info("EQ Additional Info - Name: {}, Value: {}", attr.name, attr.value));
        }

    }

    private void clearArrayAndIterator(EquipmentOrHolderList_THolder terminationPointList, EquipmentOrHolderIterator_IHolder terminationPointIterator) {
        clearArrayAndElements(terminationPointList);  // Clear the array and its elements
        clearIteratorReference(terminationPointIterator);  // Clear the iterator reference
    }

    private void clearIteratorReference(EquipmentOrHolderIterator_IHolder terminationPointIterator) {
        if (terminationPointIterator != null) {
            terminationPointIterator.value = null;  // Nullify the iterator reference
        }
    }

    private void clearArrayAndElements(EquipmentOrHolderList_THolder terminationPointList) {
        if (terminationPointList != null) {
            if (terminationPointList.value != null) {
                for (int i = 0; i < terminationPointList.value.length; i++) {
                    terminationPointList.value[i] = null;  // Nullify each element
                }
            }
            terminationPointList.value = null;  // Nullify the array reference
        }
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
        return equipmentRepository.truncate();
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
        return DiscoveryItemType.EQUIPMENT;
    }
}
