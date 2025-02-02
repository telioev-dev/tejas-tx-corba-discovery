package com.teliolabs.corba.runners;

import com.teliolabs.corba.data.service.DataManagerFactory;
import com.teliolabs.corba.data.service.DataManagerService;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

@Log4j2
public class NiaApplicationRunner implements ApplicationRunner {

    private final DataManagerService dataManagerService;

    public NiaApplicationRunner() {
        this.dataManagerService = DataManagerFactory.getDataManagerService();
    }


    @Override
    public void run(Map<String, String> args) throws Exception {
        log.info("NiaApplicationRunner starts.");
        dataManagerService.discoverNia();
    }
}
