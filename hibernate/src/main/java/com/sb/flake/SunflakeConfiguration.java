package com.sb.flake;

import com.sb.flake.annotations.FlakePreset;
import com.sb.flake.exception.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.helius.commons.SmartProperties;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Global default configuration for the Sunflake module.
 */
public final class SunflakeConfiguration {
    public static final String FILE_NAME = "sunflake.properties";
    private static final String PREFIX = "sunflake.";
    public static final String PRESET = PREFIX + "preset";
    public static final String PER_TABLE_GENERATOR = PREFIX + "perTableGenerator";

    public static final String SEQUENCE_SIZE = PREFIX + "sequenceSize";
    public static final String WORKER_ID_SIZE = PREFIX + "workerIdSize";

    public static final String TIMESTAMP = PREFIX + "timestamp.";
    public static final String TIMESTAMP_SIZE = TIMESTAMP + "size";
    public static final String TIMESTAMP_UNIT = TIMESTAMP + "unit";
    public static final String TIMESTAMP_UNITS_PER_TICK = TIMESTAMP_UNIT + "unitsPerTick";
    public static final String TIMESTAMP_ALLOW_USAGE_OF_SIGN_BIT = TIMESTAMP_UNIT + "allowUsageOfSignBit";
    public static final String EPOCH_PROPERTY = PREFIX + "epoch";

    private static GenerationRules globalRules = null;
    private static Instant epoch = null;

    private SunflakeConfiguration() {}

    /**
     * Return the global rules.
     *
     * @return the global rules.
     */
    public static synchronized GenerationRules getGlobalRules() {
        if (globalRules == null) {
            initialize();
        }
        return globalRules;
    }

    /**
     * Return the global epoch.
     *
     * @return the global epoch.
     */
    public static synchronized Instant getEpoch() {
        if (epoch == null) {
            initialize();
        }
        return epoch;
    }

    private static synchronized void initialize() {
        if (globalRules != null && epoch != null) return;

        SmartProperties properties = readProperties();
        try {
            if (globalRules == null) {
                Optional<FlakePreset> preset = properties.getEnum(PRESET, FlakePreset.class);
                globalRules = preset.map(FlakePreset::getRules)
                        .orElseGet(() -> {
                            var rules = new GenerationRulesBuilder();
                            properties.ifIntPresent(SEQUENCE_SIZE, rules::setSequenceSize)
                                    .ifIntPresent(WORKER_ID_SIZE, rules::setWorkerIdSize)
                                    .ifIntPresent(TIMESTAMP_SIZE, rules::setTimestampSize)
                                    .ifBooleanPresent(TIMESTAMP_ALLOW_USAGE_OF_SIGN_BIT, rules::setAllowUsageOfSignBit)
                                    .ifEnumPresent(TIMESTAMP_UNIT, TimeUnit.class, rules::setTimeUnit);
                            return rules.build();
                        });
            }
        } catch (IllegalArgumentException e) {
            throw new InitializationException(e);
        }
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
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath() + FILE_NAME;
        try {
            var read = new SmartProperties();
            read.load(new FileInputStream(path));
            return read;
        } catch (IOException | IllegalArgumentException e) {
            throw new InitializationException("Exception while reading the properties file at: " + path, e);
        }
    }
}
