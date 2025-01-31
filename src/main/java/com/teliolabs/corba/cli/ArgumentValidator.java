package com.teliolabs.corba.cli;

import com.teliolabs.corba.application.types.DiscoveryItemType;
import com.teliolabs.corba.application.types.ExecutionMode;
import com.teliolabs.corba.exception.InvalidArgumentException;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class ArgumentValidator {

    /**
     * Validates that all required arguments are present.
     *
     * @param cmdArgs      the CommandLineArgs object containing arguments
     * @param requiredArgs the required argument keys
     */
    public static void validateArguments(CommandLineParser cmdArgs, CommandLineArg... requiredArgs) {
        log.info("Arguments received: {}", cmdArgs);
        List<String> missingArgs = new ArrayList<>();

        // Validate unconditional arguments
        for (CommandLineArg requiredArg : requiredArgs) {
            if (cmdArgs.get(requiredArg) == null) {
                missingArgs.add(requiredArg.getKey());
            }
        }

        // Conditional validation: If `job` is "import", then `entity` is mandatory
        String jobValue = cmdArgs.get(CommandLineArg.JOB);
        if (ExecutionMode.IMPORT == ExecutionMode.fromValue(jobValue)) {
            if (cmdArgs.get(CommandLineArg.ENTITY) == null) {
                missingArgs.add(CommandLineArg.ENTITY.getKey());
            }
        } else if (ExecutionMode.DELTA == ExecutionMode.fromValue(jobValue)) {
            if (cmdArgs.get(CommandLineArg.DELTA_DAYS_BEFORE) == null) {
                missingArgs.add(CommandLineArg.DELTA_DAYS_BEFORE.getKey());
            }
        }

        if (!missingArgs.isEmpty()) {
            log.error("Missing arguments: {}. Please specify them using '--<key>=<value>'.", String.join(", ", missingArgs));
            System.exit(1); // Exit with a non-zero status for missing arguments
        }
        String entity = cmdArgs.get(CommandLineArg.ENTITY);
        if (DiscoveryItemType.fromValue(entity) == null) {
            System.exit(2);
        }
    }
}

