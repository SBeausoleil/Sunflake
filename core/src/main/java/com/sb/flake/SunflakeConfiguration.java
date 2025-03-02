package com.sb.flake;

import systems.helius.commons.SmartProperties;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Global default configuration for the Sunflake module whn used with an ORM.
 */
public class SunflakeConfiguration {

    public static final String FILE_NAME = "sunflake.properties";
    private static final String PREFIX = "sunflake.";
    public static final String PRESET = PREFIX + "preset";
    public static final String EPOCH_PROPERTY = PREFIX + "epoch";

    public static final String SEQUENCE_SIZE = PREFIX + "sequenceSize";
    public static final String WORKER_ID_SIZE = PREFIX + "workerIdSize";

    public static final String WORK_ID_SOURCE = PREFIX + "workerIdSource";

    public static final String TIMESTAMP = PREFIX + "timestamp.";
    public static final String TIMESTAMP_SIZE = TIMESTAMP + "size";
    /**
     * The time unit of the timestamp.
     * The accepted values are those of the {@link TimeUnit} enum.
     */
    public static final String TIMESTAMP_UNIT = TIMESTAMP + "unit";
    public static final String TIMESTAMP_UNITS_PER_TICK = TIMESTAMP_UNIT + "unitsPerTick";
    public static final String TIMESTAMP_ALLOW_USAGE_OF_SIGN_BIT = TIMESTAMP_UNIT + "allowUsageOfSignBit";

    private static GenerationRules globalRules;
    private static Long workerId;
    private static Instant epoch;

    private SunflakeConfiguration() {
    }

    /**
     * Return the global rules.
     * <p>
     * If they are not already set, the sunflake.properties file will be read.
     * </p>
     *
     * @return the global rules.
     */
    public static GenerationRules getGlobalRules() {
        if (globalRules == null) {
            initialize();
        }
        return globalRules;
    }

    public static Instant getEpoch() {
        if (epoch == null) {
            initialize();
        }
        return epoch;
    }

    public static long getWorkerId() {
        if (workerId == null) {
            initialize();
        }
        return workerId;
    }

    /**
     * Initialize the configuration using the given properties.
     * @param props
     */
    // Default encapsulation is to make it reachable for tests
    static void initialize(SmartProperties props) {
        try {
            readEpoch(props);
            readRules(props);
            readWorkerId(props);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new InitializationException(e);
        }
    }

    /**
     * Reset the configuration.
     * <p>
     * This will reset the global rules, worker ID and epoch.
     * </p>
     */
    // Default encapsulation is to make it reachable for tests
    static void reset() {
        globalRules = null;
        workerId = null;
        epoch = null;
    }

    private static synchronized void initialize() {
        if (globalRules == null || workerId == null || epoch == null) {
            SmartProperties properties = readProperties();
            initialize(properties);
        }
    }

    private static void readRules(SmartProperties properties) {
        if (globalRules == null) {
            Optional<FlakePreset> preset = properties.getEnum(PRESET, FlakePreset.class);
            globalRules = preset.map(flakePreset -> flakePreset.getRules(epoch))
                    .orElseGet(() -> {
                        var rules = new GenerationRulesBuilder(epoch);
                        properties.ifIntPresent(SEQUENCE_SIZE, rules::setSequenceSize)
                                .ifIntPresent(WORKER_ID_SIZE, rules::setWorkerIdSize)
                                .ifIntPresent(TIMESTAMP_SIZE, rules::setTimestampSize)
                                .ifIntPresent(TIMESTAMP_UNITS_PER_TICK, rules::setTimeUnitsPerTick)
                                .ifBooleanPresent(TIMESTAMP_ALLOW_USAGE_OF_SIGN_BIT, rules::setAllowUsageOfSignBit)
                                .ifEnumPresent(TIMESTAMP_UNIT, TimeUnit.class, rules::setTimeUnit);
                        return rules.build();
                    });
        }
    }

    private static void readWorkerId(SmartProperties properties) {
        if (workerId == null) {
            String workerIdSourceClass = properties.getProperty(WORK_ID_SOURCE);
            if (workerIdSourceClass != null) {
                try {
                    Class<?> clazz = Class.forName(workerIdSourceClass);
                    WorkerIdSupplier supplier = (WorkerIdSupplier) clazz.getMethod("getInstance", SmartProperties.class)
                            .invoke(null, readProperties());
                    workerId = supplier.getWorkerId(globalRules.getWorkerSize());
                } catch (Exception e) {
                    throw new InitializationException("Exception while reading the worker ID from the class: " + workerIdSourceClass, e);
                }
            } else {
                throw new InitializationException("Worker ID source is not set. Set a worker ID source for the property " + WORK_ID_SOURCE + " in the Sunflake configuration file.");
            }
        }
    }

    private static void readEpoch(SmartProperties properties) {
        if (epoch == null) {
            String epochProp = properties.getProperty(EPOCH_PROPERTY);
            if (epochProp != null) {
                try {
                    epoch = LocalDate.parse(epochProp)
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC);
                } catch (DateTimeParseException e) {
                    throw new InitializationException("Epoch property is incorrectly formatted. Make sure it follows the yyyy-MM-dd format.");
                }
            } else {
                throw new InitializationException("Epoch property is not set. Set an epoch for the property " + EPOCH_PROPERTY + " in the Sunflake configuration file.");
            }
        }
    }

    private static SmartProperties readProperties() {
        String path = System.getProperty("sunflake.file");
        if (path == null) {
            path = FILE_NAME;
        }
        try {
            var read = new SmartProperties();
            read.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
            return read;
        } catch (IOException | IllegalArgumentException e) {
            throw new InitializationException("Exception while reading the properties file at: " + path, e);
        }
    }

    /**
     * Parse the given ID using the global rules.
     *
     * @param id the ID to parse
     * @return the parsed data
     */
    public static FlakeData parse(long id) {
        return getGlobalRules().parse(id);
    }
}
