package com.sb;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class SnowflakeGeneratorTest {

    @Test
    void nextId_setsBitsCorrectly() {
        final int MACHINE_ID = ThreadLocalRandom.current().nextInt() & ((1 << SnowflakeGenerator.MACHINE_ID_LENGTH) - 1);
        SnowflakeGenerator generator = new SnowflakeGenerator(MACHINE_ID, Instant.now());

        long snowflake = generator.nextId();

        FlakeData data = generator.parse(snowflake);
        String flakeDefinition = " Snowflake was: " + snowflake + "(" + SnowflakeGenerator.toFormattedBinary(snowflake) + "), parsed: " + data;
        assertFalse(data.getSinceEpoch().isNegative(), "Negative duration." + flakeDefinition);
        assertTrue(data.getSinceEpoch().toMillis() <= 2, "Weird duration on single call." + flakeDefinition); // Allow 2ms room for very slow computers
        assertEquals(MACHINE_ID, data.getMachineId(), "Invalid machineId." + flakeDefinition);
        assertEquals(0, data.getSequenceNumber(), "invalid sequence number." + flakeDefinition);
    }

    // Because random testing found an issue when the timestamp started by 1 and was not shifted correctly.
    @Test
    void nextId_setsTimestampCorrectly() {
        final int LOW_MACHINE_ID = 2;
        for (int i = 0; i < 100; i++) {
            SnowflakeGenerator generator = new SnowflakeGenerator(LOW_MACHINE_ID, Instant.now());

            long snowflake = generator.nextId();

            FlakeData data = generator.parse(snowflake);
            String flakeDefinition = " Snowflake was: " + snowflake + "(" + SnowflakeGenerator.toFormattedBinary(snowflake) + "), parsed: " + data;
            assertEquals(LOW_MACHINE_ID, data.getMachineId(), "Invalid machineId." + flakeDefinition);

            System.out.println("OK!" + flakeDefinition);
        }
    }

}