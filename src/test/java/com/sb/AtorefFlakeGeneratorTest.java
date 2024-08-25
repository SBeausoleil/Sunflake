package com.sb;

import com.sb.flake.AtorefFlakeGenerator;
import com.sb.flake.FlakeGenerator;
import com.sb.flake.GenerationRules;

import java.time.Instant;

public class AtorefFlakeGeneratorTest extends FlakeGeneratorTestSuite {
    @Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId) {
        return new AtorefFlakeGenerator(epoch, machineId, GenerationRules.SNOWFLAKE);
    }
}
