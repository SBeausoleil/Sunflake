package com.sb;

import com.sb.flake.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.time.Instant;

public class FlakeGeneratorBenchmark {

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 1, time =  1)
    public void measureSingleCall_1Thread(Blackhole bh, HighFrequencyExecutionPlan plan) {
        bh.consume(plan.generator.nextId());
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 1, time =  1)
    @Threads(2)
    public void measureSingleCall_2Threads(Blackhole bh, HighFrequencyExecutionPlan plan) {
        bh.consume(plan.generator.nextId());
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 1, time =  1)
    @Threads(4)
    public void measureSingleCall_4Threads(Blackhole bh, HighFrequencyExecutionPlan plan) {
        bh.consume(plan.generator.nextId());
    }

    @Benchmark
    @Fork(value = 1, warmups = 1)
    @Warmup(iterations = 1, time = 1)
    @Measurement(iterations = 1, time =  1)
    @Threads(12)
    public void measureSingleCall_12Threads(Blackhole bh, HighFrequencyExecutionPlan plan) {
        bh.consume(plan.generator.nextId());
    }

    @State(Scope.Benchmark)
    public static class HighFrequencyExecutionPlan {
        @Param({"Atoref", "Synchronized"})
        public String implementation;
        public FlakeGenerator generator;

        private FlakeGenerator fromImplementation(String implementation) {
            switch (implementation) {
                case "Atoref":
                    return new AtorefFlakeGenerator(Instant.now(), 1L, GenerationRules.VERY_HIGH_FREQUENCY);
                case "Synchronized":
                    return new SynchronizedFlakeGenerator(Instant.now(), 1L, GenerationRules.VERY_HIGH_FREQUENCY);
                default: throw new IllegalArgumentException("Unknown implementation: " + implementation);
            }
        }

        @Setup
        public void setup() {
            generator = fromImplementation(implementation);
        }
    }
}
