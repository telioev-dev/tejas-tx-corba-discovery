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
public class ImportApplicationRunner implements ApplicationRunner {

    private final DataManagerService dataManagerService;

    public ImportApplicationRunner() {
        this.dataManagerService = DataManagerFactory.getDataManagerService();
    }

    @Override
    public void run(Map<String, String> args) throws Exception {
        DiscoveryItemType discoveryItemType = ExecutionContext.getInstance().getEntity();
        log.info("ImportApplicationRunner starts for DiscoveryItemType: {}", discoveryItemType);
        updateJobState(JobState.RUNNING);
        if (DiscoveryItemType.ME == discoveryItemType) {
            dataManagerService.discoverManagedElements(DiscoverySource.NMS, ExecutionMode.IMPORT);
        } else if (DiscoveryItemType.EQUIPMENT == discoveryItemType) {
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverEquipments(DiscoverySource.NMS, ExecutionMode.IMPORT);
        } else if (DiscoveryItemType.PTP == discoveryItemType) {
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTerminationPoints(DiscoverySource.NMS, ExecutionMode.IMPORT);
        } else if (DiscoveryItemType.TOPOLOGY == discoveryItemType) {
            log.info("Import Topologies will start with discovering ME from DB and PTP from DB");
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTerminationPoints(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTopologies(DiscoverySource.NMS, ExecutionMode.IMPORT);
        } else if (DiscoveryItemType.SNC == discoveryItemType) {
            log.info("Import SNCs will start with discovering ME from DB and PTP from DB");
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTerminationPoints(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverSubnetworkConnections(DiscoverySource.NMS, ExecutionMode.IMPORT);
        } else if (DiscoveryItemType.SNC_PACKET == discoveryItemType) {
            log.info("Import Packet SNCs will start with discovering ME from DB and PTP from DB");
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTerminationPoints(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverSubnetworkConnections(DiscoverySource.NMS, ExecutionMode.IMPORT);
        } else if (DiscoveryItemType.ROUTE == discoveryItemType) {
            log.info("Import Routes will start with discovering ME from DB and PTP from DB");
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTerminationPoints(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverSubnetworkConnections(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverRoutes(DiscoverySource.NMS, ExecutionMode.IMPORT);
        } else if (DiscoveryItemType.ROUTE_PACKET == discoveryItemType) {
            log.info("Import Packet Routes s will start with discovering ME from DB and PTP from DB");
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTerminationPoints(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverSubnetworkConnections(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverRoutes(DiscoverySource.NMS, ExecutionMode.IMPORT);
        } else if (DiscoveryItemType.FDFR == discoveryItemType) {
            log.info("Import Packet Routes s will start with discovering ME from DB and PTP from DB");
            dataManagerService.discoverManagedElements(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTerminationPoints(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverFDFR(DiscoverySource.NMS, ExecutionMode.IMPORT);
        } else if (DiscoveryItemType.ALL == discoveryItemType) {
            //dataManagerService.startFullDiscovery();
        } else if (DiscoveryItemType.NIA_VIEW == discoveryItemType) {
            dataManagerService.discoverNia();
        } else if (DiscoveryItemType.TRAIL == discoveryItemType) {
            dataManagerService.discoverSubnetworkConnections(DiscoverySource.DB, ExecutionMode.IMPORT);
            dataManagerService.discoverTrails();
        }
    }
}
