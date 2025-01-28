package com.teliolabs.corba;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.cli.CommandLineParser;
import com.teliolabs.corba.data.domain.TopologyEntity;
import com.teliolabs.corba.data.dto.Circle;
import com.teliolabs.corba.data.dto.Topology;
import com.teliolabs.corba.data.repository.CircleRepository;
import com.teliolabs.corba.data.repository.TopologyRepository;
import com.teliolabs.corba.data.service.CircleService;
import com.teliolabs.corba.data.service.TopologyService;
import com.teliolabs.corba.utils.TopologyUtils;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class TopologyTest {

    private static String USER_LABEL = "CITYGOLDRNC-2ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ-I14-STM-1#1 ZZZZZZZZ-AHM-GJ-CHS-045_TMU=05_STM1No=1ZZZZZZZZZZZZZ-ZZZZZZZZZZZZZZZZZZZZ";

    public static void main(String[] args) {
        initializeExecutionContext();
        TopologyService topologyService = TopologyService.getInstance(TopologyRepository.getInstance());
        log.info("topologyService {}", topologyService);
        List<Topology> topologies = topologyService.findAllTopologies();
        log.info("Topologies count : {}", topologies.size());
        for (Topology topology : topologies) {
            //log.info("NativeEmsName- {}, Label- {}, A_END_ME- {}, Z_END_ME- {}",
            //      topology.getNativeEmsName(), topology.getUserLabel(), topology.getAEndMeName(), topology.getZEndMeName());
            if (topology.getUserLabel().equals(USER_LABEL)) {
                log.info("GOTCHA");
                log.info("LABEL: {}", topology.getUserLabel());
                TopologyUtils.deriveTopologyType(topology);
            }

        }
    }

    private static void initializeExecutionContext() {
        CircleRepository circleRepository = CircleRepository.getInstance();
        CircleService circleService = CircleService.getInstance(circleRepository);
        Circle circle = circleService.findByNameAndVendor("GJ", "ECI");
        log.info("circle: {}", circle);
        ExecutionContext executionContext = ExecutionContext.getInstance();
        executionContext.setCircle(circle);
    }
}

