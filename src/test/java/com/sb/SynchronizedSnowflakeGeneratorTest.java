package com.sb;

import com.sb.flake.FlakeGenerator;
import com.sb.flake.GenerationRules;
import com.sb.flake.SynchronizedFlakeGenerator;

import java.time.Instant;

public class SynchronizedSnowflakeGeneratorTest extends FlakeGeneratorTestSuite {
    @Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId) {
        return new SynchronizedFlakeGenerator(epoch, machineId, GenerationRules.SNOWFLAKE);
    }
}
