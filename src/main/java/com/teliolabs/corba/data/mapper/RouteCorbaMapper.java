package com.teliolabs.corba.data.mapper;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.dto.Route;
import com.teliolabs.corba.data.dto.SNC;
import com.teliolabs.corba.data.holder.ManagedElementHolder;
import com.teliolabs.corba.data.holder.SNCHolder;
import org.tmforum.mtnm.subnetworkConnection.CrossConnect_T;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class RouteCorbaMapper implements CorbaMapper<CrossConnect_T, Route> {


    // Singleton instance
    private static final RouteCorbaMapper INSTANCE = new RouteCorbaMapper();

    // Private constructor to enforce Singleton
    private RouteCorbaMapper() {
    }

    // Public method to get the instance
    public static RouteCorbaMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public Route mapFromCorba(CrossConnect_T input) {
        ZonedDateTime executionTimestamp = ExecutionContext.getInstance().getExecutionTimestamp();
        Map<String, SNC> sncMap = SNCHolder.getInstance().getElements();
        return null;
    }

    @Override
    public Map<String, Route> toMap(List<Route> list) {
        return null;
    }

    @Override
    public List<Route> mapFromCorbaList(List<CrossConnect_T> list) {
        return null;
    }
}
