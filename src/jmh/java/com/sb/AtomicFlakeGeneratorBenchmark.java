package com.sb;

import com.sb.flake.AtomicFlakeGenerator;
import com.sb.flake.FlakeGenerator;
import com.sb.flake.GenerationRules;

import java.time.Instant;

public class AtomicFlakeGeneratorBenchmark /*extends FlakeGeneratorBenchmark */{
    //@Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId, GenerationRules rules) {
        return new AtomicFlakeGenerator(epoch, machineId, rules);
    }
}
