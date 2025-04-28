package com.sb.flake;

import org.junit.jupiter.api.Test;
import systems.helius.commons.SmartProperties;

import static org.junit.jupiter.api.Assertions.*;

class EnvPropWorkerIdSupplierTest {

    /**
     * Test
     */
    @Test
    void GivenEnvironmentVariable_WhenGetIdWithHashedProperty_ThenProvideId() {
        final String PROPERTY = "JAVA_HOME"; // All machines that can run this test have this environment property
        var properties = new SmartProperties();
        properties.put(EnvPropWorkerIdSupplier.PROPERTY_NAME, PROPERTY);
        properties.put(EnvPropWorkerIdSupplier.HASH_PROPERTY, "true"); // The java home is not a number, so hash it

        var supplier = EnvPropWorkerIdSupplier.getInstance(properties);
        long workerId = supplier.getWorkerId(32); // This way the result of hashCode that is a 32 bit int fits without modification

        assertEquals(System.getenv(PROPERTY).hashCode(), workerId);
    }
}