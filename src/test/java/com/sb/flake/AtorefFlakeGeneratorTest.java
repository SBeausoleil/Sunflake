package com.sb.flake;

import java.time.Instant;

public class AtorefFlakeGeneratorTest extends FlakeGeneratorTestSuite {
    @Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId) {
        return new AtorefFlakeGenerator(epoch, machineId, GenerationRules.SNOWFLAKE);
    }
}
