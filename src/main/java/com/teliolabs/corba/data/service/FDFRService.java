package com.teliolabs.corba.data.service;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.mapper.FDFRCorbaMapper;
import com.teliolabs.corba.data.mapper.SNCCorbaMapper;
import com.teliolabs.corba.data.repository.FDFRRepository;
import com.teliolabs.corba.discovery.DiscoveryService;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.transport.CorbaErrorHandler;
import com.teliolabs.corba.utils.CorbaConstants;
import lombok.extern.log4j.Log4j2;
import org.tmforum.mtnm.flowDomain.FDIterator_I;
import org.tmforum.mtnm.flowDomain.FDIterator_IHolder;
import org.tmforum.mtnm.flowDomain.FDList_THolder;
import org.tmforum.mtnm.flowDomain.FlowDomain_T;
import org.tmforum.mtnm.flowDomainFragment.FDFrIterator_I;
import org.tmforum.mtnm.flowDomainFragment.FDFrIterator_IHolder;
import org.tmforum.mtnm.flowDomainFragment.FDFrList_THolder;
import org.tmforum.mtnm.flowDomainFragment.FlowDomainFragment_T;
import org.tmforum.mtnm.globaldefs.NameAndStringValue_T;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;
import org.tmforum.mtnm.subnetworkConnection.SubnetworkConnection_T;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Log4j2
public class FDFRService implements DiscoveryService {

    private static FDFRService instance;
    private final FDFRRepository fdfrRepository;
    private Integer discoveryCount = 0;
    private long start;
    private long end;


    // Public method to get the singleton instance
    public static FDFRService getInstance(FDFRRepository sncRepository) {
        if (instance == null) {
            synchronized (FDFRService.class) {
                if (instance == null) {
                    instance = new FDFRService(sncRepository);
                    log.debug("FDFR Service instance created.");
                }
            }
        }
        return instance;
    }

    private FDFRService(FDFRRepository fdfrRepository) {
        this.fdfrRepository = fdfrRepository;
    }

    private NameAndStringValue_T[] buildDeltaSearchCriteria(FlowDomain_T flowDomainT) {
        if (flowDomainT == null || flowDomainT.name == null || flowDomainT.name.length == 0) {
            log.warn("FlowDomain is null or has no name attributes");
            return new NameAndStringValue_T[0];
        }

        log.info("Building delta search criteria for FDFR discovery.");
        String emsName = null;
        String flowDomainName = null;

        for (NameAndStringValue_T s : flowDomainT.name) {
            if (CorbaConstants.EMS_STR.equalsIgnoreCase(s.name)) {
                emsName = s.value;
            } else if (CorbaConstants.FLOW_DOMAIN_STR.equalsIgnoreCase(s.name)) {
                flowDomainName = s.value;
            }
        }

        return new NameAndStringValue_T[]{
                new NameAndStringValue_T(CorbaConstants.EMS_STR, emsName),
                new NameAndStringValue_T(CorbaConstants.FLOW_DOMAIN_STR, flowDomainName),
                new NameAndStringValue_T(CorbaConstants.TIMESTAMP_SIGNATURE_STR, ExecutionContext.getInstance().getDeltaTimestamp())
        };
    }

    public void discoverFDFR(CorbaConnection corbaConnection, ExecutionMode executionMode) throws SQLException, ProcessingFailureException {

        try {
            List<FlowDomain_T> flowDomainTs = fetchAllFlowDomains(corbaConnection);
            for (FlowDomain_T flowDomainT : flowDomainTs) {
                logFlowDomainDetails(flowDomainT);
                processFlowDomain(flowDomainT, corbaConnection);
            }
            updateJobStatus();
        } catch (Exception e) {
            log.error("Error during SNC discovery: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void processFlowDomain(FlowDomain_T flowDomainT, CorbaConnection corbaConnection) throws ProcessingFailureException, SQLException {
        int HOW_MANY = 100;
        FDFrList_THolder fdFrListTHolder = new FDFrList_THolder();
        FDFrIterator_IHolder fdFrIteratorIHolder = new FDFrIterator_IHolder();
        short[] connectionRateList = {};
        try {
            start = System.currentTimeMillis();
            corbaConnection.getFlowDomainMgr().
                    getAllFDFrs(ExecutionMode.DELTA == ExecutionContext.getInstance().getExecutionMode()
                                    ? buildDeltaSearchCriteria(flowDomainT) : flowDomainT.name,
                            HOW_MANY, connectionRateList, fdFrListTHolder, fdFrIteratorIHolder);
            FlowDomainFragment_T[] flowDomainFragmentTs = fdFrListTHolder.value;
            log.info("Discovered FlowDomainFragment_T: {}", flowDomainFragmentTs.length);
            discoveryCount = discoveryCount + flowDomainFragmentTs.length;
            flowDomainFragmentTs = null;
            processFDFR(fdFrListTHolder, fdFrIteratorIHolder, corbaConnection);
            end = System.currentTimeMillis();
            printDiscoveryResult(end - start);
        } catch (ProcessingFailureException e) {
            CorbaErrorHandler.handleProcessingFailureException(e, "processFlowDomain: " + Arrays.toString(flowDomainT.name));
            log.error("Failed to process subnetwork: {}", Arrays.toString(flowDomainT.name), e);
            throw e;
        }
    }

    private void processFDFR(FDFrList_THolder fdFrListTHolder, FDFrIterator_IHolder fdFrIteratorIHolder, CorbaConnection corbaConnection) throws ProcessingFailureException, SQLException {
        int batchSize = ExecutionContext.getInstance().getCircle().getTopologyHowMuch();
        FDFrIterator_I iterator = fdFrIteratorIHolder.value;
        if (iterator != null) {
            boolean hasMoreData = true;
            try {
                while (hasMoreData) {
                    hasMoreData = iterator.next_n(batchSize, fdFrListTHolder);
                    discoveryCount = discoveryCount + fdFrListTHolder.value.length;
                    FlowDomainFragment_T[] flowDomainFragmentTs = fdFrListTHolder.value;
                    if (isExecutionModeImport()) {
                        saveFDFR(flowDomainFragmentTs);
                    } else {

                    }
                    flowDomainFragmentTs = null;
                    if (discoveryCount % 1000 == 0) {
                        log.info("Discovered FDFs so far: {}", discoveryCount);
                    }
                }
            } catch (Exception e) {
                log.error("Error fetching SNCs : {}", e.getMessage(), e);
                throw e;
            }
        }
    }


    private void logFlowDomainDetails(FlowDomain_T flowDomainT) {
        Arrays.stream(flowDomainT.name).forEach(attr ->
                log.info("FlowDomain Attribute - Name: {}, Value: {}", attr.name, attr.value));
        Arrays.stream(flowDomainT.additionalInfo).forEach(attr ->
                log.info("FlowDomain Additional Info - Name: {}, Value: {}", attr.name, attr.value));
    }

    private List<FlowDomain_T> fetchAllFlowDomains(CorbaConnection corbaConnection) throws ProcessingFailureException {
        List<FlowDomain_T> flowDomainTs = new ArrayList<>();
        FDList_THolder fdListTHolder = new FDList_THolder();
        FDIterator_IHolder fdIteratorIHolder = new FDIterator_IHolder();

        corbaConnection.getFlowDomainMgr().getAllFlowDomains(10, fdListTHolder, fdIteratorIHolder);
        Collections.addAll(flowDomainTs, fdListTHolder.value);

        if (fdIteratorIHolder.value != null) {
            FDIterator_I iterator = fdIteratorIHolder.value;
            while (iterator.next_n(10, fdListTHolder)) {
                Collections.addAll(flowDomainTs, fdListTHolder.value);
            }
        }
        return flowDomainTs;
    }

    public void runDeltaProcess(CorbaConnection corbaConnection) throws SQLException, ProcessingFailureException {
        discoverFDFR(corbaConnection, ExecutionMode.DELTA);
    }


    private void saveFDFR(FlowDomainFragment_T[] flowDomainFragmentT) throws SQLException {
        long start = System.currentTimeMillis();
        long end = System.currentTimeMillis();
        FDFRCorbaMapper.getInstance().mapFromCorbaArray(flowDomainFragmentT);
        flowDomainFragmentT = null;
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
        return 0;
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

}
