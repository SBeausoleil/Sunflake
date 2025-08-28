package com.sb.flake;

import com.sb.flake.exceptions.InitializationException;
import com.sb.flake.util.WorkerIdSupplierUtil;
import jakarta.annotation.Nullable;
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
    static final String PREFIX = "sunflake.";
    public static final String PRESET = PREFIX + "preset";
    public static final String EPOCH_PROPERTY = PREFIX + "epoch";

    public static final String SEQUENCE_SIZE = PREFIX + "sequenceSize";
    public static final String WORKER_ID_SIZE = PREFIX + "workerIdSize";

    public static final String WORKER_ID_SOURCE = PREFIX + "workerIdSource";
    /**
     * Source of worker ID to use if the one provided by the preferred worker id ({@link SunflakeConfiguration#WORKER_ID_SOURCE}
     * provided an ID in use.
     * <p>
     *     Default: null
     * </p>
     * <p>
     *     If the sunflake-hibernate module is on the classpath, we recommend using "com.sb.flake.NextOpenId".
     * </p>
     */
    public static final String FALLBACK_WORKER_ID_SOURCE = PREFIX + "fallbackWorkerIdSource";

    public static final String TIMESTAMP = PREFIX + "timestamp.";
    public static final String TIMESTAMP_SIZE = TIMESTAMP + "size";
    /**
     * The time unit of the timestamp.
     * The accepted values are those of the {@link TimeUnit} enum.
     */
    public static final String TIMESTAMP_UNIT = TIMESTAMP + "unit";
    public static final String TIMESTAMP_UNITS_PER_TICK = TIMESTAMP_UNIT + "unitsPerTick";
    public static final String TIMESTAMP_ALLOW_USAGE_OF_SIGN_BIT = TIMESTAMP_UNIT + "allowUsageOfSignBit";

    /**
     * How long to reserve the worker ID for this process, in seconds.<br/>
     * Recommended to be at least 60 seconds.<br/>
     * Default is 150 seconds (2 minutes and 30 seconds).<br/>
     * <p>
     *     Must be greater than the renewal interval.
     * </p>
     */
    public static final String RESERVE_DURATION_SECONDS_PROPERTY = PREFIX + "keepAlive";
    public static final int DEFAULT_RESERVE_DURATION_SECONDS = 150;

    /**
     * How often to renew the worker ID reservation, in seconds.<br/>
     * Recommended to be at least 30 seconds.<br/>
     * Default is 120 seconds (2 minutes).<br/>
     * <p>
     *     Must be less than the keep alive interval.
     * </p>
     */
    public static final String RENEWAL_INTERVAL_SECONDS_PROPERTY = PREFIX + "renewalInterval";
    public static final int DEFAULT_RENEWAL_INTERVAL_SECONDS = 120;

    public static final String WORKER_ID_RESERVATION_MAX_TRIES_PROPERTY = PREFIX + "workerIdReservationMaxTries";
    /**
     * How many times to try alternative worker IDs when the desired one is already taken.
     */
    public static final int DEFAULT_WORKER_ID_RESERVATION_MAX_TRIES = 10;

    private static GenerationRules globalRules;
    private static WorkerIdSupplier preferredWorkerIdSupplier;
    @Nullable
    private static WorkerIdSupplier fallbackWorkerIdSupplier;
    private static Long workerId;
    @Nullable
    private static AliveMarker aliveMarker;
    private static Instant epoch;

    /**
     * The duration in seconds for which a worker ID is reserved.
     * Only useful when using a {@link KeepAliveService}.
     */
    private static int reserveDuration = -1;
    /**
     * The interval in seconds at which the reservation is renewed.
     * Only useful when using a {@link KeepAliveService}.
     */
    private static int renewalInterval = -1;
    /**
     * The maximum number of tries to acquire a worker ID.
     * Only useful when using a {@link KeepAliveService}.
     */
    private static int maxTries = -1;

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

    public static int getReserveDuration() {
        if (reserveDuration == -1) {
            initialize();
        }
        return reserveDuration;
    }

    public static int getRenewalInterval() {
        if (renewalInterval == -1) {
            initialize();
        }
        return renewalInterval;
    }

    public static int getMaxTries() {
        if (maxTries == -1) {
            initialize();
        }
        return maxTries;
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
        reserveDuration = -1;
        renewalInterval = -1;
        maxTries = -1;
    }

    private static synchronized void initialize() {
        if (globalRules == null || workerId == null || epoch == null
                || reserveDuration == -1 || renewalInterval == -1 || maxTries == -1) {
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
        if (reserveDuration == -1) {
            reserveDuration = properties.getInt(RESERVE_DURATION_SECONDS_PROPERTY, DEFAULT_RESERVE_DURATION_SECONDS);
        }
        if (renewalInterval == -1) {
            renewalInterval = properties.getInt(RENEWAL_INTERVAL_SECONDS_PROPERTY, DEFAULT_RENEWAL_INTERVAL_SECONDS);
        }
        if (maxTries == -1) {
            maxTries = properties.getInt(WORKER_ID_RESERVATION_MAX_TRIES_PROPERTY, DEFAULT_WORKER_ID_RESERVATION_MAX_TRIES);
        }
    }

    private static void readWorkerId(SmartProperties properties) {
        if (workerId == null) {
            String workerIdSourceClass = properties.getProperty(WORKER_ID_SOURCE);
            if (workerIdSourceClass != null) {
                try {
                    preferredWorkerIdSupplier = WorkerIdSupplierUtil.getWorkerIdSupplier(workerIdSourceClass, readProperties());
                    workerId = preferredWorkerIdSupplier.getWorkerId(globalRules.getWorkerSize());
                } catch (NoSuchMethodException e) {
                    throw new InitializationException(e.getMessage(), e);
                } catch (Exception e) {
                    throw new InitializationException("Exception while reading the worker ID from the class: " + workerIdSourceClass, e);
                }
            } else {
                throw new InitializationException("Worker ID source is not set. Set a worker ID source for the property " + WORKER_ID_SOURCE + " in the Sunflake configuration file.");
            }
        }
        if (fallbackWorkerIdSupplier == null) {
            String fallbackWorkerIdClass = properties.getProperty(FALLBACK_WORKER_ID_SOURCE);
            if (fallbackWorkerIdClass != null) {
                try {
                    fallbackWorkerIdSupplier = WorkerIdSupplierUtil.getWorkerIdSupplier(fallbackWorkerIdClass, readProperties());
                } catch (NoSuchMethodException e) {
                    throw new InitializationException(e.getMessage(), e);
                } catch (Exception e) {
                    throw new InitializationException("Exception while reading the fallback worker ID source from the class: " + fallbackWorkerIdClass, e);
                }
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
     * Returns the liveliness marker, if the keep alive service has been initialized.
     * @return the liveliness marker, if it exists.
     */
    public static Optional<AliveMarker> getAliveMarker() {
        return Optional.ofNullable(aliveMarker);
    }

    public static WorkerIdSupplier getPreferredWorkerIdSupplier() {
        return preferredWorkerIdSupplier;
    }

    public static Optional<WorkerIdSupplier> getFallbackWorkerIdSupplier() {
        return Optional.ofNullable(fallbackWorkerIdSupplier);
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

    public static void setAliveMarker(AliveMarker aliveMarker) {
        SunflakeConfiguration.aliveMarker = aliveMarker;
        if (aliveMarker != null && aliveMarker.getWorkerId() != workerId) {
            workerId = aliveMarker.getWorkerId();
            GeneratorsRegistry.getInstance().setWorkerId(workerId);
        }
    }
}
