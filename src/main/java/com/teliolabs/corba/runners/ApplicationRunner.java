package com.teliolabs.corba.runners;

import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.domain.JobEntity;
import com.teliolabs.corba.application.repository.JobRepository;
import com.teliolabs.corba.application.service.JobService;
import com.teliolabs.corba.application.types.JobState;

import java.util.Map;

@FunctionalInterface
public interface ApplicationRunner {
    void run(Map<String, String> args) throws Exception;

    default void updateJobState(JobState jobState) {
        JobRepository jobRepository = JobRepository.getInstance();
        JobService jobService = JobService.getInstance(jobRepository);
        JobEntity currentJob = ExecutionContext.getInstance().getCurrentJob();
        currentJob.setJobState(jobState);
        jobService.updateJobStatus(currentJob);
    }
}
