package com.teliolabs.corba;


import com.teliolabs.corba.application.ExecutionContext;
import com.teliolabs.corba.application.JobInitializer;
import com.teliolabs.corba.application.domain.JobEntity;
import com.teliolabs.corba.application.repository.JobRepository;
import com.teliolabs.corba.application.service.JobService;
import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
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

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;

@Log4j2
public class TxCorbaDiscovery {

    public static final ZonedDateTime NOW = ZonedDateTime.now();
    public static final String DELTA_TIMESTAMP = DateTimeUtils.getDeltaTimestamp(1);

    public static void main(String[] args) {

        log.info("TxCorbaDiscoveryApplication application starting");

        log.debug("Arguments validated.");
        CommandLineParser cmdArgs = new CommandLineParser(args);

        // Validate required arguments
        ArgumentValidator.validateArguments(cmdArgs, CommandLineArg.JOB, CommandLineArg.CIRCLE, CommandLineArg.VENDOR);

        // Extract arguments
        String job = cmdArgs.get(CommandLineArg.JOB);
        String circleName = cmdArgs.get(CommandLineArg.CIRCLE);
        String vendorName = cmdArgs.get(CommandLineArg.VENDOR);
        String entity = cmdArgs.get(CommandLineArg.ENTITY);

        String logFileName = String.format("%s-%s-%s.log", vendorName.toLowerCase(), circleName.toLowerCase(), job.toLowerCase());

        System.setProperty("log.fileName", logFileName);

        // Reinitialize Log4j2
        Configurator.reconfigure();


        // Initialize services
        CircleRepository circleRepository = CircleRepository.getInstance();
        CircleService circleService = CircleService.getInstance(circleRepository);

        // Set up execution context
        ExecutionContext executionContext = initializeExecutionContext(circleService, circleName, vendorName);

        JobInitializer.initializeJob(cmdArgs);

        JobRepository jobRepository = JobRepository.getInstance();
        JobService jobService = JobService.getInstance(jobRepository);

        // Execute the appropriate runner based on the "job" argument
        try {
            executeRunner(job, cmdArgs, executionContext);
            JobEntity jobEntity = ExecutionContext.getInstance().getCurrentJob();
            jobEntity.setEndTimestamp(Timestamp.from(Instant.now()));
            jobService.updateJob(ExecutionContext.getInstance().getCurrentJob());
        } catch (Exception e) {
            JobEntity jobEntity = ExecutionContext.getInstance().getCurrentJob();
            jobEntity.setEndTimestamp(Timestamp.from(Instant.now()));
            jobEntity.setErrorMessage(e.getMessage());
            jobService.updateJob(ExecutionContext.getInstance().getCurrentJob());
        }
    }

    private static ExecutionContext initializeExecutionContext(CircleService circleService, String circleName, String vendorName) {
        try {
            Circle circle = circleService.findByNameAndVendor(circleName, vendorName);
            log.debug("Found Circle: {}", circle);

            ExecutionContext executionContext = ExecutionContext.getInstance();
            executionContext.setCircle(circle);
            Thread haltedHook = new Thread(() -> {
                log.info("Shutdown hook called, closing any existing Corba connection.");
                try {
                    CorbaConnection.getConnection(circle).close();
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

    private static void executeRunner(String job, CommandLineParser cmdArgs, ExecutionContext executionContext) {
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

