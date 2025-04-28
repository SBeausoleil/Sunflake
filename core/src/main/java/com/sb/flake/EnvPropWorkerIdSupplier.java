package com.sb.flake;

import systems.helius.commons.SmartProperties;

/**
 * Worker ID supplier that reads the system's environment properties.
 */
public class EnvPropWorkerIdSupplier implements WorkerIdSupplier {
    private static final String PREFIX = SunflakeConfiguration.PREFIX + "workerId.";
    /**
     * (String) Name of the system environment property that is read.
     */
    public static final String PROPERTY_NAME = PREFIX + "property_name";
    /**
     * (boolean [default: false]) Whether to hash the value of the property or parse it directly as a number.
     */
    public static final String HASH_PROPERTY = PREFIX + "hash";

    private final long workerId;

    public EnvPropWorkerIdSupplier(String propertyName, boolean hash) {
        String property = System.getenv(propertyName);
        if (hash) {
            workerId = property.hashCode();
        } else {
            workerId = Long.parseLong(propertyName);
        }
    }

    public static WorkerIdSupplier getInstance(SmartProperties properties) {
        String propertyName = properties.getProperty(PROPERTY_NAME);
        boolean hash = properties.getBoolean(HASH_PROPERTY, false);
        return new EnvPropWorkerIdSupplier(propertyName, hash);
    }

    @Override
    public long getWorkerId(int maxLength) {
        return workerId % (1L << maxLength);
    }
}
