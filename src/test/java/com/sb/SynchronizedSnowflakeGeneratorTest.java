package com.sb;

import com.sb.flake.FlakeGenerator;
import com.sb.flake.Snowflake;
import com.sb.flake.SynchronizedFlakeGenerator;

import java.time.Instant;

public class SynchronizedSnowflakeGeneratorTest extends FlakeGeneratorTestSuite {
    @Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId) {
        return new SynchronizedFlakeGenerator(epoch, machineId, Snowflake.RULES);
    }
}
