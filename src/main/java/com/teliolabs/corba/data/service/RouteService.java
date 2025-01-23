package com.teliolabs.corba.data.service;

import com.teliolabs.corba.data.dto.SNC;
import com.teliolabs.corba.data.repository.SNCRepository;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.subnetworkConnection.SNCIterator_IHolder;
import org.tmforum.mtnm.subnetworkConnection.SubnetworkConnectionList_THolder;
import org.tmforum.mtnm.subnetworkConnection.SubnetworkConnection_T;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class RouteService {

    private static RouteService instance;
    private final SNCRepository sncRepository;

    private SubnetworkConnectionList_THolder sncList = new SubnetworkConnectionList_THolder();
    private SNCIterator_IHolder sncIterator = new SNCIterator_IHolder();
    private List<SubnetworkConnection_T> subnetworkConnectionTList = new ArrayList<>();
    private List<SNC> sncs = new ArrayList<>();


    // Public method to get the singleton instance
    public static RouteService getInstance(SNCRepository sncRepository) {
        if (instance == null) {
            synchronized (RouteService.class) {
                if (instance == null) {
                    instance = new RouteService(sncRepository);
                    log.debug("SNCService instance created.");
                }
            }
        }
        return instance;
    }

    private RouteService(SNCRepository sncRepository) {
        this.sncRepository = sncRepository;
    }


}
