package com.sb.flake;

import java.time.Instant;

public class SynchronizedSnowflakeGeneratorTest extends FlakeGeneratorTestSuite {
    @Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId) {
        return new SynchronizedFlakeGenerator(epoch, machineId, GenerationRules.SNOWFLAKE);
    }

    @Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId, GenerationRules rules) {
        return new SynchronizedFlakeGenerator(epoch, machineId, rules);
    }
}
