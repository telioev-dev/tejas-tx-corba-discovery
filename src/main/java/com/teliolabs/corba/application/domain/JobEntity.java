package com.teliolabs.corba.application.domain;

import com.teliolabs.corba.application.types.JobState;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.sql.Timestamp;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
public abstract class JobEntity {
    protected Long jobId;
    protected String duration;
    protected String vendor;
    protected String circle;
    private String runningUser;
    protected Timestamp startTimestamp;
    private Timestamp endTimestamp;
    private JobState jobState;
    private String errorMessage;
}
