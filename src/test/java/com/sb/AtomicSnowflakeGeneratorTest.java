package com.sb;

import com.sb.flake.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class AtomicSnowflakeGeneratorTest extends FlakeGeneratorTestSuite {
    @Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId) {
        return new AtomicFlakeGenerator(epoch, machineId, GenerationRules.SNOWFLAKE);
    }
}