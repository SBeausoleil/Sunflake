package com.sb;

import com.sb.flake.AtorefFlakeGenerator;
import com.sb.flake.FlakeGenerator;
import com.sb.flake.GenerationRules;

import java.time.Instant;

public class AtorefFlakeGeneratorBenchmark /*extends FlakeGeneratorBenchmark*/ {
    //@Override
    FlakeGenerator makeGenerator(Instant epoch, long machineId, GenerationRules rules) {
        return new AtorefFlakeGenerator(epoch, machineId, rules);
    }
}
