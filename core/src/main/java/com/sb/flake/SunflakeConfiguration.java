package com.sb.flake;

import systems.helius.commons.SmartProperties;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

/**
 * Global default configuration for the Sunflake module.
 */
public class SunflakeConfiguration {

    public static final Instant DEFAULT_EPOCH = LocalDate.of(2025, 1, 1).atStartOfDay(ZoneOffset.UTC).toInstant();

    public static final String FILE_NAME = "sunflake.properties";
    private static final String PREFIX = "sunflake.";
    public static final String PRESET = PREFIX + "preset";
    public static final String PER_TABLE_GENERATOR = PREFIX + "perTableGenerator";
    public static final String EPOCH_PROPERTY = PREFIX + "epoch";

    public static final String SEQUENCE_SIZE = PREFIX + "sequenceSize";
    public static final String WORKER_ID_SIZE = PREFIX + "workerIdSize";

    public static final String TIMESTAMP = PREFIX + "timestamp.";
    public static final String TIMESTAMP_SIZE = TIMESTAMP + "size";
    public static final String TIMESTAMP_UNIT = TIMESTAMP + "unit";
    public static final String TIMESTAMP_UNITS_PER_TICK = TIMESTAMP_UNIT + "unitsPerTick";
    public static final String TIMESTAMP_ALLOW_USAGE_OF_SIGN_BIT = TIMESTAMP_UNIT + "allowUsageOfSignBit";

    private static GenerationRules globalRules = null;
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

    public static Instant getEpochProperty() {
        if (epoch == null) {
            initialize();
        }
        return epoch;
    }

    /**
     * Set the rules that all new SunflakeProvider used by Hibernate will use.
     *
     * @param globalRules
     */
    public static synchronized void setGlobalRules(GenerationRules globalRules) {
        globalRules = globalRules;
    }

    private static synchronized void initialize() {
        if (globalRules == null || epoch == null) {
            try {
                SmartProperties properties = readProperties();
                readRules(properties);
                readEpoch(properties);
            } catch (IllegalArgumentException | DateTimeParseException e) {
                throw new InitializationException(e);
            }
        }
    }

    private static void readEpoch(SmartProperties properties) {
        if (epoch == null) {
            String epochProperty = properties.getProperty(EPOCH_PROPERTY);
            if (epochProperty != null) {
                epoch = LocalDate.parse(epochProperty, DateTimeFormatter.ISO_LOCAL_DATE)
                        .atStartOfDay(ZoneOffset.UTC).toInstant();
            } else {
                epoch = DEFAULT_EPOCH;
            }
        }
    }

    private static void readRules(SmartProperties properties) {
        if (globalRules == null) {
            FlakePreset preset = properties.getEnum(PRESET, FlakePreset.class);
            if (preset != null) {
                globalRules = preset.getRules();
            } else {
                var rules = new GenerationRulesBuilder();
                properties.ifIntPresent(SEQUENCE_SIZE, rules::setSequenceSize)
                        .ifIntPresent(WORKER_ID_SIZE, rules::setWorkerIdSize)
                        .ifIntPresent(TIMESTAMP_SIZE, rules::setTimestampSize)
                        .ifBooleanPresent(TIMESTAMP_ALLOW_USAGE_OF_SIGN_BIT, rules::setAllowUsageOfSignBit)
                        .ifEnumPresent(TIMESTAMP_UNIT, TimeUnit.class, rules::setTimeUnit);
                globalRules = rules.build();
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
}
