package com.sb.flake;

import java.time.Instant;

public class SynchronizedSnowflakeGeneratorTest extends FlakeGeneratorTestSuite {
    @Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId) {
        return new SynchronizedFlakeGenerator(epoch, machineId, GenerationRules.SNOWFLAKE);
    }
}
