package com.teliolabs.corba.data.service;

import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.repository.RouteRepository;
import com.teliolabs.corba.transport.CorbaConnection;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RouteService {

    private static RouteService instance;
    private final RouteRepository routeRepository;

    // Public method to get the singleton instance
    public static RouteService getInstance(RouteRepository routeRepository) {
        if (instance == null) {
            synchronized (RouteService.class) {
                if (instance == null) {
                    instance = new RouteService(routeRepository);
                    log.debug("RouteService instance created.");
                }
            }
        }
        return instance;
    }

    private RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public void discoverRoutes(CorbaConnection corbaConnection, ExecutionMode executionMode) {

    }

    public void runDeltaProcess(CorbaConnection corbaConnection) {

    }

}
