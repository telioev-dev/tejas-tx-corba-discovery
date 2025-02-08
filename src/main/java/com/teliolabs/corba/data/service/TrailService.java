package com.teliolabs.corba.data.service;

import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.data.dto.ManagedElement;
import com.teliolabs.corba.data.dto.SNC;
import com.teliolabs.corba.data.holder.SNCHolder;
import com.teliolabs.corba.data.repository.TrailRepository;
import com.teliolabs.corba.discovery.DiscoveryService;
import com.teliolabs.corba.transport.CorbaConnection;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j2
public class TrailService implements DiscoveryService {

    private static TrailService instance;
    private final TrailRepository trailRepository;

    // Public method to get the singleton instance
    public static TrailService getInstance(TrailRepository trailRepository) {
        if (instance == null) {
            synchronized (TrailService.class) {
                if (instance == null) {
                    instance = new TrailService(trailRepository);
                    log.info("TrailService instance created.");
                }
            }
        }
        return instance;
    }

    private TrailService(TrailRepository trailRepository) {
        this.trailRepository = trailRepository;
    }


    public void generateTrailsFromSncAndRoutes() {
        Map<String, SNC> sncMap = SNCHolder.getInstance().getElements();
        if (sncMap == null || sncMap.isEmpty()) {
            throw new IllegalStateException("No SNCs to generate trails for");
        }
        Set<String> sncIdMap = sncMap.keySet();
        for (String sncId : sncIdMap) {
            SNC snc = sncMap.get(sncId);
            String vCat = snc.getVCat();
            short sncRate = snc.getSncRate();
            if (!"0".equals(vCat) && sncRate != 309) {
                log.info("Processing snc_id for EOS: {}", sncId);
                trailRepository.createEoSTrails(sncId);
            } else if (sncRate == 309 || sncId.toLowerCase().startsWith("/mpls")) {
                log.info("Processing snc_id for PKT: {}", sncId);
            } else {
                log.info("Processing snc_id for SDH: {}", sncId);
                trailRepository.createSDHTrails(sncId);
            }
        }
    }

    @Override
    public int discover(CorbaConnection corbaConnection) {
        throw new IllegalStateException("Not Supported.");
    }

    @Override
    public int discoverDelta(CorbaConnection corbaConnection) {
        throw new IllegalStateException("Not Supported.");
    }

    @Override
    public int deleteAll() {
        return 0;
    }

    @Override
    public int getDiscoveryCount() {
        return 0;
    }

    @Override
    public long getStartDiscoveryTimestampInMillis() {
        return 0;
    }

    @Override
    public long getEndDiscoveryTimestampInMillis() {
        return 0;
    }

    @Override
    public DiscoveryItemType getDiscoveryItemType() {
        return DiscoveryItemType.TRAIL;
    }
}
