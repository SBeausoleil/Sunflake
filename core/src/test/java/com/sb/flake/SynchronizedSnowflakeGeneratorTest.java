package com.sb.flake;

import java.time.Instant;

public class SynchronizedSnowflakeGeneratorTest extends FlakeGeneratorTestSuite {
    @Override
    FlakeGenerator makeGenerator(long machineId) {
        return new SynchronizedFlakeGenerator(machineId, GenerationRules.snowflake(Instant.now()));
    }

    @Override
    FlakeGenerator makeGenerator(long machineId, GenerationRules rules) {
        return new SynchronizedFlakeGenerator(machineId, rules);
    }
}
