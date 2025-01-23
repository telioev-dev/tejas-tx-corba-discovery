package com.teliolabs.corba.data.service;

import com.teliolabs.corba.TxCorbaDiscoveryApplication;
import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.ExecutionContextAware;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.domain.EquipmentEntity;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.holder.EquipmentHolder;
import com.teliolabs.corba.data.holder.ManagedElementHolder;
import com.teliolabs.corba.data.mapper.EquipmentCorbaMapper;
import com.teliolabs.corba.data.repository.EquipmentRepository;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.utils.CorbaConstants;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.equipment.EquipmentInventoryMgr_I;
import org.tmforum.mtnm.equipment.EquipmentOrHolderIterator_IHolder;
import org.tmforum.mtnm.equipment.EquipmentOrHolderList_THolder;
import org.tmforum.mtnm.equipment.EquipmentOrHolder_T;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;
import org.tmforum.mtnm.terminationPoint.TerminationPoint_T;

import java.sql.SQLException;
import java.util.*;

@Log4j2
public class EquipmentService implements ExecutionContextAware {

    private static EquipmentService instance;
    private final EquipmentRepository equipmentRepository;
    private NameAndStringValue_T[] neNameArray;
    private EquipmentInventoryMgr_I eiManager;
    private List<EquipmentOrHolder_T> equipmentOrHolderTList = new ArrayList<>();
    private Integer discoveredEquipmentCount = 0;


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
            neNameArray[2] = new NameAndStringValue_T(CorbaConstants.TIMESTAMP_SIGNATURE_STR, TxCorbaDiscoveryApplication.DELTA_TIMESTAMP);
        }
    }


    public void runDeltaProcess() {

    }

    public void discoverEquipments() throws ProcessingFailureException, SQLException {
        Map<String, ManagedElement> managedElements = ManagedElementHolder.getInstance().getElements();
        if (managedElements != null && !managedElements.isEmpty()) {
            Set<String> meNamesSet = managedElements.keySet();
            int i = 0;
            for (String meName : meNamesSet) {
                if (meName.contains("UME")) continue; // IGNORE UMEs for Equipment

                log.info("#{} ME '{}' for Equipment processing.", i++, meName);
                try {
                    processManagedElement(meName);
                } catch (ProcessingFailureException e) {
                    log.error("Error occurred during network calls for Equipments");
                    log.error("Error reason: " + e.errorReason);
                    log.error("Error type: " + e.exceptionType.value());
                    throw e;
                } catch (Exception e) {
                    log.error("Error occurred during network calls for Equipments");
                    throw e;
                }
            }
        } else {
            log.error("Equipment discovery can't run as no MEs found in the DB");
            return;
        }

        log.debug("Total Equipments fetched from NMS: {}", discoveredEquipmentCount);

        // TODO: Remove this later after testing.. no longer need after we switched to save then and there
//        if (equipmentOrHolderTList != null && !equipmentOrHolderTList.isEmpty() && ExecutionMode.IMPORT == getExecutionMode()) {
//            try {
//                saveEquipments();
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }

    private void saveEquipments() throws SQLException {
        log.info("Converting Corba Equipments to DTOs");
        List<EquipmentEntity> equipmentList = EquipmentCorbaMapper.getInstance().mapFromCorbaList(equipmentOrHolderTList);

        long start = System.currentTimeMillis();
        equipmentRepository.insertEquipments(equipmentList, 100);
        long end = System.currentTimeMillis();
        log.info("Successfully saved {} Termination Points in {} seconds.", equipmentOrHolderTList.size(), (end - start) / 1000);
        saveInMemory(equipmentList);
    }

    private void saveEquipments(List<EquipmentOrHolder_T> equipmentOrHolderTList) throws SQLException {
        List<EquipmentEntity> equipmentList = EquipmentCorbaMapper.getInstance().mapFromCorbaList(equipmentOrHolderTList);
        long start = System.currentTimeMillis();
        equipmentRepository.insertEquipments(equipmentList, 50);
        long end = System.currentTimeMillis();
        log.debug("Successfully saved {} Equipments in {} seconds.", equipmentOrHolderTList.size(), (end - start) / 1000);
    }

    private void saveInMemory(List<EquipmentEntity> equipmentList) {
        log.debug("Saving Equipments in memory: {}", (equipmentList != null && !equipmentList.isEmpty()));
        EquipmentHolder.getInstance().setElements(EquipmentCorbaMapper.getInstance().toMap(equipmentList));
        equipmentList = null;
    }

    private void processManagedElement(String meName) throws ProcessingFailureException, SQLException {
        neNameArray[1].value = meName;
        List<EquipmentOrHolder_T> equipmentOrHolderTList = new ArrayList<>();
        int HOW_MANY = ExecutionContext.getInstance().getCircle().getPtpHowMuch();
        EquipmentOrHolderList_THolder equipOrHolderList = new EquipmentOrHolderList_THolder();
        EquipmentOrHolderIterator_IHolder equipOrHolderItr = new EquipmentOrHolderIterator_IHolder();
        eiManager.getAllEquipment(neNameArray, HOW_MANY, equipOrHolderList, equipOrHolderItr);
        log.debug("getAllEquipment: got {} EQs for ME {}.", equipOrHolderList.value.length, neNameArray[1].value);
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
        discoveredEquipmentCount = discoveredEquipmentCount + equipmentOrHolderTList.size();
        log.info("getAllEquipment: total EQs discovered so far {}.", discoveredEquipmentCount);
        saveEquipments(equipmentOrHolderTList);
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

}
