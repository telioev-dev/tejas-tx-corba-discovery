package com.teliolabs.corba.application;

import com.teliolabs.corba.application.domain.JobEntity;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.data.dto.Circle;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class ExecutionContext {

    private Circle circle;
    private ExecutionMode executionMode;
    private DiscoveryItemType entity;
    private String entityId;
    private JobEntity currentJob;
    private ZonedDateTime executionTimestamp;
    private String deltaTimestamp;
    // Singleton instance
    private static final ExecutionContext INSTANCE = new ExecutionContext();

    // Private constructor to enforce Singleton
    private ExecutionContext() {
    }

    // Public method to get the instance
    public static ExecutionContext getInstance() {
        return INSTANCE;
    }
}
