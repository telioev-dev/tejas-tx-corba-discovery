package com.teliolabs.corba.data.service;

import com.teliolabs.corba.data.repository.ManagedElementRepository;
import com.teliolabs.corba.data.repository.PTPRepository;
import com.teliolabs.corba.data.repository.SNCRepository;
import com.teliolabs.corba.data.repository.TopologyRepository;

public class DataManagerFactory {

    private static DataManagerService dataManagerService;

    private DataManagerFactory() {
        // Private constructor to prevent instantiation
    }

    public static synchronized DataManagerService getDataManagerService() {
        if (dataManagerService == null) {
            ManagedElementRepository managedElementRepository = ManagedElementRepository.getInstance();
            ManagedElementService managedElementService = ManagedElementService.getInstance(managedElementRepository);

            TopologyRepository topologyRepository = TopologyRepository.getInstance();
            TopologyService topologyService = TopologyService.getInstance(topologyRepository);

            PTPRepository ptpRepository = PTPRepository.getInstance();
            PTPService ptpService = PTPService.getInstance(ptpRepository);

            SNCRepository sncRepository = SNCRepository.getInstance();
            SNCService sncService = SNCService.getInstance(sncRepository);

            dataManagerService = DataManagerService.getInstance(managedElementService, topologyService, ptpService, sncService);
        }
        return dataManagerService;
    }
}

