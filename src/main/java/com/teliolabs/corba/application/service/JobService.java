package com.teliolabs.corba.application.service;

import com.teliolabs.corba.application.domain.DeltaJobEntity;
import com.teliolabs.corba.application.domain.ImportJobEntity;
import com.teliolabs.corba.application.domain.JobEntity;
import com.teliolabs.corba.application.repository.JobRepository;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JobService {

    private static JobService instance;

    // Private constructor to enforce Singleton
    private JobService() {
    }


    // Public method to get the instance
    public static JobService getInstance(JobRepository jobRepository) {
        if (instance == null) {
            synchronized (JobService.class) {
                if (instance == null) {
                    instance = new JobService(jobRepository);
                    log.debug("JobService instance created.");
                }
            }
        }
        return instance;
    }

    private JobRepository jobRepository;

    // Constructor for dependency injection
    private JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public ImportJobEntity createImportJob(ImportJobEntity importJob) {
        return jobRepository.insertImportJob(importJob);
    }

    public DeltaJobEntity createDeltaJob(DeltaJobEntity deltaJob) {
        return jobRepository.insertDeltaJob(deltaJob);
    }

    public void updateJob(JobEntity job) {
        if (job instanceof ImportJobEntity) {
            jobRepository.updateImportJob((ImportJobEntity) job);
        } else if (job instanceof DeltaJobEntity) {
            jobRepository.updateDeltaJob((DeltaJobEntity) job);
        } else {
            throw new IllegalArgumentException("Unsupported job type: " + job.getClass());
        }
    }

    public void updateJobStatus(JobEntity job) {
        if (job instanceof ImportJobEntity) {
            jobRepository.updateImportJobStatus((ImportJobEntity) job);
        } else if (job instanceof DeltaJobEntity) {
            jobRepository.updateDeltaJobStatus((DeltaJobEntity) job);
        } else {
            throw new IllegalArgumentException("Unsupported job type: " + job.getClass());
        }
    }
}

