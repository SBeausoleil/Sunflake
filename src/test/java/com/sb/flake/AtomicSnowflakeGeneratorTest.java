package com.sb.flake;

import java.time.Instant;

class AtomicSnowflakeGeneratorTest extends FlakeGeneratorTestSuite {
    @Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId) {
        return new AtomicFlakeGenerator(epoch, machineId, GenerationRules.SNOWFLAKE);
    }
}