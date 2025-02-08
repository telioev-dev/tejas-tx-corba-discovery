package com.teliolabs.corba.data.service;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.domain.ManagedElementEntity;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.holder.ManagedElementHolder;
import com.teliolabs.corba.data.mapper.ManagedElementCorbaMapper;
import com.teliolabs.corba.data.mapper.ManagedElementResultSetMapper;
import com.teliolabs.corba.data.repository.ManagedElementRepository;
import com.teliolabs.corba.discovery.DiscoveryService;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.utils.CollectionUtils;
import com.teliolabs.corba.utils.ManagedElementUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;
import org.tmforum.mtnm.managedElement.ManagedElementIterator_IHolder;
import org.tmforum.mtnm.managedElement.ManagedElementList_THolder;
import org.tmforum.mtnm.managedElement.ManagedElement_T;
import org.tmforum.mtnm.managedElementManager.ManagedElementMgr_I;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@RequiredArgsConstructor
public class ManagedElementService implements DiscoveryService {

    private static ManagedElementService instance;
    private final ManagedElementRepository managedElementRepository;
    private Integer discoveryCount;
    private boolean insertedInDb;
    private long start;
    private long end;

    // Public method to get the singleton instance
    public static ManagedElementService getInstance(ManagedElementRepository managedElementRepository) {
        if (instance == null) {
            synchronized (ManagedElementService.class) {
                if (instance == null) {
                    instance = new ManagedElementService(managedElementRepository);
                    log.debug("ManagedElementService instance created.");
                }
            }
        }
        return instance;
    }

    public static ManagedElementService getInstance() {
        return instance;
    }


    @Getter
    private List<ManagedElement_T> managedElements = new ArrayList<>();
    private ManagedElementMgr_I meManager;

    public void runDeltaProcess(CorbaConnection corbaConnection) throws Exception {
        List<ManagedElement_T> managedElementTs = discover(corbaConnection, ExecutionMode.DELTA);
        if (managedElementTs == null || managedElementTs.isEmpty()) {
            log.warn("No MEs discovered from NMS!");
            return;
        }


        List<ManagedElementEntity> managedElementEntities = managedElementRepository.findAllManagedElements(false);
        if (managedElementEntities == null || managedElementEntities.isEmpty()) {
            log.warn("No MEs found in the DB during Delta, let's insert!");
            try {
                saveManagedElements();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else {
            log.info("Discovered MEs (NMS): {}, Existing MEs (DB): {}", managedElementTs.size(), managedElementEntities.size());
            softDeleteManagedElements(managedElementTs, managedElementEntities);
//            try {
//                managedElementRepository.upsertManagedElements(ManagedElementCorbaMapper.getInstance().mapFromCorbaList(managedElementTs));
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
        }


    }

    public void loadAll() {
        boolean excludeDeleted = isExecutionModeDelta() && DiscoveryItemType.ME != ExecutionContext.getInstance().getEntity();
        List<ManagedElement> managedElementList = managedElementRepository.
                findAllManagedElements(ManagedElementResultSetMapper.getInstance()::mapToDto, excludeDeleted);
        if (managedElementList != null && !managedElementList.isEmpty()) {
            log.info("Total MEs {} loaded from DB", managedElementList.size());
            ManagedElementHolder.getInstance().setElements(CollectionUtils.convertListToMap(managedElementList, ManagedElement::getMeName));
            managedElementList = null;
        } else {
            log.error("No MEs found to be loaded from the DB");
        }
    }

    private void softDeleteManagedElements(List<ManagedElement_T> managedElementTs, List<ManagedElementEntity> managedElementEntities) {
        // Find the me_name values that exist in dbList but not in corbaList
        Set<String> corbaMeNames = managedElementTs.stream().map(me -> ManagedElementUtils.getMEName(me.name)).collect(Collectors.toSet());

        List<String> meNamesToDelete = managedElementEntities.stream().filter(dbEntity -> !corbaMeNames.contains(dbEntity.getMeName())).
                map(ManagedElementEntity::getMeName).collect(Collectors.toList());

        if (!meNamesToDelete.isEmpty()) {
            log.info("Found {} MEs that were deleted from NMS, marking them deleted in the DB.", meNamesToDelete.size());
            log.info("To be deleted MEs: {}", meNamesToDelete);
            managedElementRepository.deleteManagedElements(meNamesToDelete, true);
        } else {
            log.info("No MEs were found to be deleted from NMS, hence exiting.");
        }
    }


    public List<ManagedElement_T> discover(CorbaConnection corbaConnection, ExecutionMode executionMode) throws Exception {
        meManager = corbaConnection.getMeManager();
        ManagedElementList_THolder managedElementListTHolder = new ManagedElementList_THolder();
        ManagedElementIterator_IHolder managedElementIteratorIHolder = new ManagedElementIterator_IHolder();
        int howMuch = ExecutionContext.getInstance().getCircle().getMeHowMuch();
        try {
            start = System.currentTimeMillis();
            meManager.getAllManagedElements(howMuch, managedElementListTHolder, managedElementIteratorIHolder);
            Collections.addAll(managedElements, managedElementListTHolder.value);
            boolean exitWhile = false;
            if (managedElementIteratorIHolder.value != null)
                try {
                    boolean hasMoreData = true;
                    while (hasMoreData) {
                        hasMoreData = managedElementIteratorIHolder.value.next_n(howMuch, managedElementListTHolder);
                        Collections.addAll(managedElements, managedElementListTHolder.value);
                    }
                    exitWhile = true;
                } finally {
                    if (!exitWhile) {
                        managedElementIteratorIHolder.value.destroy();
                    }
                }
            end = System.currentTimeMillis();
            discoveryCount = managedElements.size();
            printDiscoveryResult(end - start);
            updateJobStatus();
        } catch (ProcessingFailureException e) {
            throw new RuntimeException(e);
        }
        try {
            if (managedElements != null && !managedElements.isEmpty() && executionMode == ExecutionMode.IMPORT) {
                saveManagedElements();
                if (log.isDebugEnabled())
                    managedElements.forEach(this::logManagedElementDetails);
            }
        } catch (Exception e) {
            log.error("Error inserting MEs into the DB", e);
            throw e;
        }
        return managedElements;
    }

    private void logManagedElementDetails(ManagedElement_T managedElement) {
        Arrays.stream(managedElement.name).forEach(attr ->
                log.info("ME Attribute - Name: {}, Value: {}", attr.name, attr.value));
        log.info("Communication State: {}", managedElement.communicationState.value());
        Arrays.stream(managedElement.additionalInfo).forEach(attr ->
                log.info("ME Additional Info - Name: {}, Value: {}", attr.name, attr.value));
    }

    private void saveManagedElements() throws SQLException {
        int discoveredMeCount = managedElements.size();
        List<ManagedElement> managedElementList = ManagedElementCorbaMapper.getInstance().mapFromCorbaList(managedElements);
        long start = System.currentTimeMillis();
        managedElementRepository.insertManagedElements(managedElementList);
        long end = System.currentTimeMillis();
        log.info("Discovered MEs # {} inserted in {} seconds.", discoveredMeCount, (end - start) / 1000);
        insertedInDb = true;
        saveInMemory(managedElementList);
    }

    private void saveInMemory(List<ManagedElement> managedElementList) {
        ManagedElementHolder.getInstance().setElements(ManagedElementCorbaMapper.getInstance().toMap(managedElementList));
        managedElementList = null;
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
        return managedElementRepository.truncate();
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
        return DiscoveryItemType.ME;
    }
}
