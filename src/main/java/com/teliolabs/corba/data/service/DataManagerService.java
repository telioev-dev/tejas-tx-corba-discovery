package com.teliolabs.corba.data.service;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.DiscoverySource;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.repository.EquipmentRepository;
import com.teliolabs.corba.transport.CorbaConnection;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;

@Log4j2
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DataManagerService {

    private static DataManagerService instance;

    private final ManagedElementService managedElementService;
    private final TopologyService topologyService;
    private final PTPService ptpService;
    private final SNCService sncService;

    // Public method to get the singleton instance
    public static DataManagerService getInstance(ManagedElementService managedElementService,
                                                 TopologyService topologyService, PTPService ptpService, SNCService sncService) {
        if (instance == null) {
            synchronized (DataManagerService.class) {
                if (instance == null) {
                    instance = new DataManagerService(managedElementService, topologyService, ptpService, sncService);
                    log.debug("DataManagerService instance created.");
                }
            }
        }
        return instance;
    }

    public void startFullDiscovery() {
        long start = System.nanoTime();


        long startManagedElements = System.nanoTime();
        discoverManagedElements(DiscoverySource.NMS, ExecutionMode.IMPORT);
        long durationManagedElements = System.nanoTime() - startManagedElements;
        log.info("discoverManagedElements IMPORT took: {} minutes", durationManagedElements / 1_000_000_000.0 / 60);

        long startTerminationPoints = System.nanoTime();
        discoverTerminationPoints(DiscoverySource.NMS, ExecutionMode.IMPORT);
        long durationTerminationPoints = System.nanoTime() - startTerminationPoints;
        log.info("discoverTerminationPoints took: {} minutes", durationTerminationPoints / 1_000_000_000.0 / 60);

        long startTopologies = System.nanoTime();
        discoverTopologies(DiscoverySource.NMS, ExecutionMode.IMPORT);
        long durationTopologies = System.nanoTime() - startTopologies;
        log.info("discoverTopologies took: {} minutes", durationTopologies / 1_000_000_000.0 / 60);

        long startSNCs = System.nanoTime();
        discoverSubnetworkConnections(DiscoverySource.NMS, ExecutionMode.IMPORT);
        long durationSNCs = System.nanoTime() - startSNCs;
        log.info("discoverSubnetworkConnections took: {} minutes", durationSNCs / 1_000_000_000.0 / 60);

        long totalDuration = System.nanoTime() - start;
        log.info("Total Full Discovery took: {} minutes", totalDuration / 1_000_000_000.0 / 60);
    }

    public void discoverTopologies(DiscoverySource discoverySource, ExecutionMode executionMode) {
        log.info("Starting discovery of topologies with execution mode: {}", executionMode);
        if (discoverySource == DiscoverySource.DB) {

        } else {
            try (CorbaConnection corbaConnection = establishConnection()) {
                if (corbaConnection == null) return;

                if (executionMode == ExecutionMode.IMPORT) {
                    topologyService.discoverTopologies(corbaConnection, executionMode);
                } else if (executionMode == ExecutionMode.DELTA) {
                    topologyService.runDeltaProcess(corbaConnection);
                } else {
                    log.warn("Unsupported execution mode: {}", executionMode.name());
                }

            } catch (Exception e) {
                log.error("Error during fetchTopologicalLinks process: {}", ExecutionContext.getInstance().getCircle().getName(), e);
            }
        }

    }

    public void discoverEquipments(DiscoverySource discoverySource, ExecutionMode executionMode) {
        log.info("Starting discovery of Equipments with execution mode: {}", executionMode);
        EquipmentRepository equipmentRepository = EquipmentRepository.getInstance();
        if (discoverySource == DiscoverySource.DB) {

        } else {
            try (CorbaConnection corbaConnection = establishConnection()) {
                if (corbaConnection == null) return;

                EquipmentService equipmentService = EquipmentService.getInstance(equipmentRepository, corbaConnection);
                if (executionMode == ExecutionMode.IMPORT) {
                    equipmentService.discoverEquipments();
                } else if (executionMode == ExecutionMode.DELTA) {
                    equipmentService.runDeltaProcess();
                } else {
                    log.warn("Unsupported execution mode: {}", executionMode.name());
                }

            } catch (Exception e) {
                log.error("Error during discoverEquipments process: {}", ExecutionContext.getInstance().getCircle().getName(), e);
            }
        }

    }

    public void discoverSubnetworkConnections(DiscoverySource discoverySource, ExecutionMode executionMode) {
        log.info("Starting discovery of SNCs with execution mode: {}", executionMode);

        if (discoverySource == DiscoverySource.NMS) {
            try (CorbaConnection corbaConnection = establishConnection()) {
                if (corbaConnection == null) return;

                if (executionMode == ExecutionMode.IMPORT) {
                    sncService.discoverSubnetworkConnections(corbaConnection, executionMode);
                } else if (executionMode == ExecutionMode.DELTA) {
                    sncService.runDeltaProcess(corbaConnection);
                } else {
                    log.warn("Unsupported execution mode: {}", executionMode.name());
                }

            } catch (Exception e) {
                log.error("Error during discoverSubnetworkConnections process: {}", ExecutionContext.getInstance().getCircle().getName(), e);
            }
        } else {
            sncService.loadAll();
        }

    }

    public void discoverTerminationPoints(DiscoverySource discoverySource, ExecutionMode executionMode) {
        if (discoverySource == DiscoverySource.DB) {
            ptpService.loadAll();
        } else {
            try (CorbaConnection corbaConnection = establishConnection()) {
                if (corbaConnection == null) return;


                if (executionMode == ExecutionMode.IMPORT) {
                    ptpService.discoverTerminationPoints(corbaConnection, executionMode);
                } else if (executionMode == ExecutionMode.DELTA) {
                    ptpService.runDeltaProcess(corbaConnection);
                } else {
                    log.warn("Unsupported execution mode: {}", executionMode.name());
                }
            } catch (Exception e) {
                log.error("Error during discoverTerminationPoints process: {}", ExecutionContext.getInstance().getCircle().getName(), e);

            }
        }

    }

    public void startDeltaDiscovery() {
        long startManagedElements = System.nanoTime();
        discoverManagedElements(DiscoverySource.NMS, ExecutionMode.DELTA);
        long durationManagedElements = System.nanoTime() - startManagedElements;
        log.info("discoverManagedElements DELTA took: {} minutes", durationManagedElements / 1_000_000_000.0 / 60);

        long startTerminationPoints = System.nanoTime();
        discoverTerminationPoints(DiscoverySource.NMS, ExecutionMode.DELTA);
        long durationTerminationPoints = System.nanoTime() - startTerminationPoints;
        log.info("discoverTerminationPoints DELTA took: {} minutes", durationTerminationPoints / 1_000_000_000.0 / 60);

        long startTopologies = System.nanoTime();
        discoverTopologies(DiscoverySource.NMS, ExecutionMode.DELTA);
        long durationTopologies = System.nanoTime() - startTopologies;
        log.info("discoverTopologies DELTA took: {} minutes", durationTopologies / 1_000_000_000.0 / 60);

        long startSNCs = System.nanoTime();
        discoverSubnetworkConnections(DiscoverySource.NMS, ExecutionMode.DELTA);
        long durationSNCs = System.nanoTime() - startSNCs;
        log.info("discoverSubnetworkConnections DELTA took: {} minutes", durationSNCs / 1_000_000_000.0 / 60);


    }

    public void discoverManagedElements(DiscoverySource discoverySource, ExecutionMode executionMode) {
        log.info("Discover MEs with DiscoverySource: {}", discoverySource);
        if (discoverySource == DiscoverySource.NMS) {
            try (CorbaConnection corbaConnection = establishConnection()) {
                if (corbaConnection == null) return;

                if (executionMode == ExecutionMode.IMPORT) {
                    managedElementService.discoverManagedElements(corbaConnection, executionMode);
                } else if (executionMode == ExecutionMode.DELTA) {
                    managedElementService.runDeltaProcess(corbaConnection);
                } else {
                    log.warn("Unsupported execution mode: {}", executionMode.name());
                }
            } catch (Exception e) {
                log.error("Error during discoverManagedElements process: {}",
                        ExecutionContext.getInstance().getCircle().getName(), e);
            }
        } else {
            log.info("Discovery Source DB, hence trying to load all MEs from DB");
            managedElementService.loadAll();
        }

    }


    private CorbaConnection establishConnection() {
        try {
            return CorbaConnection.getConnection(ExecutionContext.getInstance().getCircle());
        } catch (ProcessingFailureException e) {
            log.error("Error reason: " + e.errorReason);
            log.error("Error type: " + e.exceptionType.value());
            log.error("Exception establishing connection to the NMS: {}",
                    ExecutionContext.getInstance().getCircle().getName(), e);
            return null;
        } catch (Exception e) {
            log.error("Exception establishing connection to the NMS: {}", ExecutionContext.getInstance().getCircle().getName(), e);
            return null;
        }
    }

}