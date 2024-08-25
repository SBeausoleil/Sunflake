package com.sb;

import com.sb.flake.FlakeGenerator;
import com.sb.flake.GenerationRules;
import com.sb.flake.SynchronizedFlakeGenerator;

import java.time.Instant;

public class SynchronizedFlakeGeneratorBenchmark /*extends FlakeGeneratorBenchmark*/ {
    //@Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId, GenerationRules rules) {
        return new SynchronizedFlakeGenerator(epoch, machineId, rules);
    }
}
