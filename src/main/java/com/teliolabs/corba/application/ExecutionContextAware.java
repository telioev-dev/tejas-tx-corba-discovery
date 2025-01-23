package com.teliolabs.corba.application;

import com.teliolabs.corba.application.domain.JobEntity;
import com.teliolabs.corba.application.repository.JobRepository;
import com.teliolabs.corba.application.service.JobService;
import com.teliolabs.corba.application.types.ExecutionMode;

public interface ExecutionContextAware {

    default ExecutionMode getExecutionMode() {
        return ExecutionContext.getInstance().getExecutionMode();
    }

    default JobEntity getCurrentJob() {
        return ExecutionContext.getInstance().getCurrentJob();
    }

    default JobService getJobService() {
        JobRepository jobRepository = JobRepository.getInstance();
        return JobService.getInstance(jobRepository);
    }

    default boolean isExecutionModeImport() {
        return ExecutionContext.getInstance().getExecutionMode() == ExecutionMode.IMPORT;
    }

    default boolean isExecutionModeDelta() {
        return ExecutionContext.getInstance().getExecutionMode() == ExecutionMode.DELTA;
    }
}


