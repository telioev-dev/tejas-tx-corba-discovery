package com.teliolabs.corba.data.service;

public class DataManagerFactory {

    private static DataManagerService dataManagerService;

    private DataManagerFactory() {
        // Private constructor to prevent instantiation
    }

    public static synchronized DataManagerService getDataManagerService() {
        if (dataManagerService == null) {
            dataManagerService = DataManagerService.getInstance();
        }
        return dataManagerService;
    }
}

