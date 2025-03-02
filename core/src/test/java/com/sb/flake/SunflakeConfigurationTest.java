package com.sb.flake;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import systems.helius.commons.SmartProperties;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static com.sb.flake.SunflakeConfiguration.*;

class SunflakeConfigurationTest {

    @BeforeEach
    void setUp() {
        SunflakeConfiguration.reset();
    }

    /**
     * Tests that the configuration file is read if the configuration is not initialized.
     */
    @Test
    void GivenUninitializedWithPresetConf_WhenGetGlobalRules_readConfAtDefault() {
        assertNotNull(SunflakeConfiguration.getGlobalRules());
        assertEquals(GenerationRules.snowflake(SunflakeConfiguration.getEpoch()),
                SunflakeConfiguration.getGlobalRules());
    }

    @Test
    void GivenUninitialized_WhenGetEpoch_readConfAtDefault() {
        assertEquals("2025-02-10T00:00:00Z", SunflakeConfiguration.getEpoch().toString());
    }

    /*
     * Test configuration file uses a random worker id generator, so we can only check that it isn't the default value.
     */
    @Test
    void GivenUninitialized_WhenGetWorkerId_readConfAtDefault() {
        assertNotEquals(0, SunflakeConfiguration.getWorkerId());
    }

    @Test
    void GivenCustomRulesToRead_WhenGetGlobalRules_returnCustomRules() {
        var props = new SmartProperties();
        props.putAll(Map.of(
                EPOCH_PROPERTY, "2012-12-30",
                SEQUENCE_SIZE, "20",
                WORKER_ID_SIZE, "20",
                TIMESTAMP_SIZE, "20",
                TIMESTAMP_UNIT, "DAYS",
                TIMESTAMP_UNITS_PER_TICK, "5",
                TIMESTAMP_ALLOW_USAGE_OF_SIGN_BIT, "true",
                WORK_ID_SOURCE, RandomWorkerIdSupplier.class.getName()
        ));
        SunflakeConfiguration.initialize(props);

        GenerationRules rules = SunflakeConfiguration.getGlobalRules();
        assertEquals(20, rules.SEQUENCE_SIZE);
        assertEquals(20, rules.WORKER_ID_SIZE);
        assertEquals(20, rules.TIMESTAMP_SIZE);
        assertEquals(TimeUnit.DAYS, rules.TIME_UNIT);
        assertEquals(5, rules.TIME_UNITS_PER_TICK);
        assertTrue(rules.ALLOW_USAGE_OF_SIGN_BIT);
        assertEquals("2012-12-30T00:00:00Z", rules.EPOCH.toString());
    }
}