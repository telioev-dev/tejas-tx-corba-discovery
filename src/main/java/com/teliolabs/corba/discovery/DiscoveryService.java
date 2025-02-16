package com.teliolabs.corba.discovery;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.ExecutionContextAware;
import com.teliolabs.corba.application.domain.DeltaJobEntity;
import com.teliolabs.corba.application.domain.ImportJobEntity;
import com.teliolabs.corba.application.domain.JobEntity;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.transport.CorbaErrorHandler;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;


public interface DiscoveryService extends ExecutionContextAware {

    static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(DiscoveryService.class);

    int discover(CorbaConnection corbaConnection); // Returns the import discovery count

    int discoverDelta(CorbaConnection corbaConnection); // Returns the delta discovery count

    int deleteAll();

    int getDiscoveryCount();

    long getStartDiscoveryTimestampInMillis();

    long getEndDiscoveryTimestampInMillis();

    default DiscoveryItemType getDiscoveryItemType() {
        return ExecutionContext.getInstance().getEntity();
    }

    default void updateJobStatus() {
        JobEntity runningJob = getCurrentJob();
        if (isExecutionModeImport()) {
            ImportJobEntity importJobEntity = (ImportJobEntity) runningJob;
            importJobEntity.setDiscoveryCount(getDiscoveryCount());
            importJobEntity.setDuration(formatDuration(getEndDiscoveryTimestampInMillis() - getStartDiscoveryTimestampInMillis()));
        } else if (isExecutionModeDelta()) {
            DeltaJobEntity deltaJobEntity = (DeltaJobEntity) runningJob;
        }
    }

    default void printDiscoveryResult(long durationInMillis) {
        String entity = getDiscoveryItemType().name();
        long elapsedTimeInSeconds = durationInMillis / 1000;
        if (elapsedTimeInSeconds >= 3600) {
            long hours = elapsedTimeInSeconds / 3600;
            long minutes = (elapsedTimeInSeconds % 3600) / 60;
            long seconds = elapsedTimeInSeconds % 60;
            log.info("Network discovery for total {}s {} took {} hours, {} minutes, and {} seconds.", entity, getDiscoveryCount(), hours, minutes, seconds);
        } else if (elapsedTimeInSeconds >= 60) {
            long minutes = elapsedTimeInSeconds / 60;
            long seconds = elapsedTimeInSeconds % 60;
            log.info("Network discovery for total {}s {} took {} minutes and {} seconds.", entity, getDiscoveryCount(), minutes, seconds);
        } else {
            log.info("Network discovery for total {}s {} took {} seconds.", entity, getDiscoveryCount(), elapsedTimeInSeconds);
        }
    }

    default String formatDuration(long durationInMillis) {
        long durationInSeconds = durationInMillis / 1000;
        if (durationInSeconds >= 3600) {
            long hours = durationInSeconds / 3600;
            long minutes = (durationInSeconds % 3600) / 60;
            long seconds = durationInSeconds % 60;
            return String.format("%d hours %d minutes %d seconds", hours, minutes, seconds);
        } else if (durationInSeconds >= 60) {
            long minutes = durationInSeconds / 60;
            long seconds = durationInSeconds % 60;
            return String.format("%d minutes %d seconds", minutes, seconds);
        } else {
            return String.format("%d seconds", durationInSeconds);
        }
    }

    default void handleProcessingFailureException(ProcessingFailureException pfe, String param)
            throws ProcessingFailureException {
        CorbaErrorHandler.handleProcessingFailureException(pfe, param);
    }
}