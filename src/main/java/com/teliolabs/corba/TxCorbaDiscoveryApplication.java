package com.teliolabs.corba;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.JobInitializer;
import com.teliolabs.corba.application.domain.JobEntity;
import com.teliolabs.corba.application.repository.JobRepository;
import com.teliolabs.corba.application.service.JobService;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.application.types.JobState;
import com.teliolabs.corba.cli.ArgumentValidator;
import com.teliolabs.corba.cli.CommandLineArg;
import com.teliolabs.corba.cli.CommandLineParser;
import com.teliolabs.corba.data.dto.Circle;
import com.teliolabs.corba.data.repository.CircleRepository;
import com.teliolabs.corba.data.service.CircleService;
import com.teliolabs.corba.runners.DeltaApplicationRunner;
import com.teliolabs.corba.runners.ImportApplicationRunner;
import com.teliolabs.corba.transport.CorbaConnection;
import com.teliolabs.corba.utils.DateTimeUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.config.Configurator;
import org.tmforum.mtnm.globaldefs.ProcessingFailureException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;

@Log4j2
public class TxCorbaDiscoveryApplication {

    public static void main(String[] args) {
        log.info("TxCorbaDiscoveryApplication application starting");

        // Parse and validate arguments
        CommandLineParser cmdArgs = parseAndValidateArguments(args);

        // Set up logging configuration
        configureLogging(cmdArgs);

        // Initialize execution context
        ExecutionContext executionContext = initializeExecutionContext(cmdArgs);

        // Initialize job
        JobEntity jobEntity = JobInitializer.initializeJob(cmdArgs);

        JobService jobService = initializeJobService();

        try {
            // Execute the appropriate runner
            executeRunner(cmdArgs.get(CommandLineArg.JOB), cmdArgs, executionContext);

            // Update job as successful
            updateJobStatus(jobEntity, jobService, null);
        } catch (Exception e) {
            // Update job as failed
            e.printStackTrace();
            updateJobStatus(jobEntity, jobService, e);
            if (e instanceof ProcessingFailureException) {
                System.exit(444);
            }
        }
    }

// Helper methods

    private static CommandLineParser parseAndValidateArguments(String[] args) {
        CommandLineParser cmdArgs = new CommandLineParser(args);
        ArgumentValidator.validateArguments(cmdArgs, CommandLineArg.JOB, CommandLineArg.CIRCLE, CommandLineArg.VENDOR, CommandLineArg.TIMESTAMP);
        return cmdArgs;
    }

    private static void configureLogging(CommandLineParser cmdArgs) {
        String job = cmdArgs.get(CommandLineArg.JOB).toLowerCase();
        String circle = cmdArgs.get(CommandLineArg.CIRCLE).toLowerCase();
        String vendor = cmdArgs.get(CommandLineArg.VENDOR).toLowerCase();

        String logFileName = String.format("%s-%s-%s.log", vendor, circle, job);
        System.setProperty("log.fileName", logFileName);
        Configurator.reconfigure();
    }

    private static ExecutionContext initializeExecutionContext(CommandLineParser cmdArgs) {
        CircleRepository circleRepository = CircleRepository.getInstance();
        CircleService circleService = CircleService.getInstance(circleRepository);
        return initializeExecutionContext(circleService, cmdArgs);
    }

    private static JobService initializeJobService() {
        JobRepository jobRepository = JobRepository.getInstance();
        return JobService.getInstance(jobRepository);
    }

    private static void updateJobStatus(JobEntity jobEntity, JobService jobService, Exception e) {
        jobEntity.setEndTimestamp(Timestamp.from(Instant.now()));
        if (e == null) {
            jobEntity.setJobState(JobState.FINISHED);
        } else {
            jobEntity.setJobState(JobState.FAILED);
            jobEntity.setErrorMessage(e.getMessage());
        }
        log.info("Final update jobEntity {}", jobEntity);
        jobService.updateJob(jobEntity);
    }


    private static ExecutionContext initializeExecutionContext(CircleService circleService, CommandLineParser cmdArgs) {
        String circleName = cmdArgs.get(CommandLineArg.CIRCLE);
        String vendorName = cmdArgs.get(CommandLineArg.VENDOR);
        String timestamp = cmdArgs.get(CommandLineArg.TIMESTAMP);
        String deltaDays = cmdArgs.get(CommandLineArg.DELTA_DAYS_BEFORE);
        String entityId = cmdArgs.get(CommandLineArg.ENTITY_ID);
        log.info("deltaDays: {}", deltaDays);
        try {
            Circle circle = circleService.findByNameAndVendor(circleName, vendorName);
            log.debug("Found Circle: {}", circle);

            ExecutionContext executionContext = ExecutionContext.getInstance();
            executionContext.setCircle(circle);
            executionContext.setExecutionTimestamp(ZonedDateTime.parse(timestamp));
            executionContext.setDeltaTimestamp(deltaDays == null ? null : DateTimeUtils.getDeltaTimestamp(Integer.parseInt(deltaDays)));
            executionContext.setEntityId(entityId);

            Thread haltedHook = new Thread(() -> {
                log.info("Shutdown hook called, closing any existing Corba connection.");
                try {
                    CorbaConnection.getConnection(circle).close();
                    log.info("Connection closed successfully from shutdown hook.");
                } catch (Exception e) {
                    log.error("Error while closing any existing connection from shutdown hook");
                    System.exit(1);
                }
            });
            Runtime.getRuntime().addShutdownHook(haltedHook);
            return executionContext;
        } catch (IllegalArgumentException e) {
            log.error("Error finding Circle: ", e);
            System.exit(1); // Exit the application with a non-zero status
            return null; // Unreachable, but needed to satisfy the compiler
        }
    }

    private static void executeRunner(String job, CommandLineParser cmdArgs, ExecutionContext executionContext) throws Exception {
        log.info("executeRunner called...");
        switch (job.toLowerCase()) {
            case "import":
                executionContext.setExecutionMode(ExecutionMode.IMPORT);
                executionContext.setEntity(DiscoveryItemType.fromValue(cmdArgs.get("entity")));
                new ImportApplicationRunner().run(cmdArgs.getArgs());
                break;
            case "delta":
                executionContext.setExecutionMode(ExecutionMode.DELTA);
                executionContext.setEntity(DiscoveryItemType.fromValue(cmdArgs.get("entity")));
                new DeltaApplicationRunner().run(cmdArgs.getArgs());
                break;
            default:
                log.error("Invalid 'job' argument: '{}'. Please specify '--job=import' or '--job=delta'.", job);
                System.exit(1);
        }
    }
}

