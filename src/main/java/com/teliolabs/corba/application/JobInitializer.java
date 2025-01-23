package com.teliolabs.corba.application;

import com.teliolabs.corba.application.domain.DeltaJobEntity;
import com.teliolabs.corba.application.domain.ImportJobEntity;
import com.teliolabs.corba.application.domain.JobEntity;
import com.teliolabs.corba.application.repository.JobRepository;
import com.teliolabs.corba.application.service.JobService;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.application.types.JobState;
import com.teliolabs.corba.cli.CommandLineArg;
import com.teliolabs.corba.cli.CommandLineParser;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JobInitializer {

    public static JobEntity initializeJob(CommandLineParser commandArgs) {

        JobRepository jobRepository = JobRepository.getInstance();
        JobService jobService = JobService.getInstance(jobRepository);
        String job = commandArgs.get(CommandLineArg.JOB);
        String vendor = commandArgs.get(CommandLineArg.VENDOR);
        String circle = commandArgs.get(CommandLineArg.CIRCLE);
        String entity = commandArgs.get(CommandLineArg.ENTITY);
        JobEntity jobEntity = null;
        if (ExecutionMode.IMPORT == ExecutionMode.fromValue(job)) {
            ImportJobEntity importJob = ImportJobEntity.builder().vendor(vendor).entity(DiscoveryItemType.fromValue(entity)).circle(circle).runningUser(System.getProperty("user.name")).jobState(JobState.NEW).build();
            jobEntity = jobService.createImportJob(importJob);
        } else if (ExecutionMode.DELTA == ExecutionMode.fromValue(job)) {
            DeltaJobEntity deltaJob = DeltaJobEntity.builder().vendor(vendor).circle(circle).runningUser(System.getProperty("user.name")).jobState(JobState.NEW).build();
            jobEntity = jobService.createDeltaJob(deltaJob);
        }

        if (jobEntity != null && jobEntity.getJobId() != null) {
            log.info("Job Initialized: {}", jobEntity);
            ExecutionContext.getInstance().setCurrentJob(jobEntity);
        }
        return jobEntity;
    }
}
