package com.teliolabs.corba.runners;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.DiscoverySource;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.application.types.JobState;
import com.teliolabs.corba.data.service.DataManagerFactory;
import com.teliolabs.corba.data.service.DataManagerService;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class DeltaApplicationRunner implements ApplicationRunner {

    private final DataManagerService dataManagerService;

    public DeltaApplicationRunner() {
        this.dataManagerService = DataManagerFactory.getDataManagerService();
    }


    @Override
    public void run(Map<String, String> args) throws Exception {
        log.info("DeltaApplicationRunner starts.");
        updateJobState(JobState.RUNNING);
        //dataManagerService.startDeltaDiscovery();
        DiscoveryItemType discoveryItemType = ExecutionContext.getInstance().getEntity();
        if (DiscoveryItemType.ME == discoveryItemType) {
            dataManagerService.discoverManagedElements(DiscoverySource.NMS, ExecutionMode.DELTA);
        } else if (DiscoveryItemType.EQUIPMENT == discoveryItemType) {
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverEquipments(DiscoverySource.NMS, ExecutionMode.DELTA);
        } else if (DiscoveryItemType.PTP == discoveryItemType) {
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTerminationPoints(DiscoverySource.NMS, ExecutionMode.DELTA);
        } else if (DiscoveryItemType.TOPOLOGY == discoveryItemType) {
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTerminationPoints(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTopologies(DiscoverySource.NMS, ExecutionMode.DELTA);
        } else if (DiscoveryItemType.SNC == discoveryItemType) {
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTerminationPoints(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverSubnetworkConnections(DiscoverySource.NMS, ExecutionMode.DELTA);
        } else if (DiscoveryItemType.ROUTE == discoveryItemType) {
            dataManagerService.discoverSubnetworkConnections(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverRoutes(DiscoverySource.NMS, ExecutionMode.DELTA);
        } else if (DiscoveryItemType.ALL == discoveryItemType) {
            //dataManagerService.startFullDiscovery();
        } else if (DiscoveryItemType.NIA_VIEW == discoveryItemType) {
            dataManagerService.discoverNia();
        }
    }
}
