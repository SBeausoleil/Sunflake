package com.sb;

import com.sb.flake.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.time.Instant;

public class FlakeGeneratorBenchmark {
    private static final int N_FORKS = 5;
    private static final int N_ITERATIONS = 3;

    @Benchmark
    @Fork(value = N_FORKS)
    @Warmup(iterations = N_ITERATIONS, time = N_ITERATIONS)
    @Measurement(iterations = N_ITERATIONS, time = N_ITERATIONS)
    public void measureSingleCall_1Thread(Blackhole bh, HighFrequencyExecutionPlan plan) {
        bh.consume(plan.generator.nextId());
    }

    @Benchmark
    @Fork(value = N_FORKS)
    @Warmup(iterations = N_ITERATIONS, time = N_ITERATIONS)
    @Measurement(iterations = N_ITERATIONS, time = N_ITERATIONS)
    @Threads(2)
    public void measureSingleCall_2Threads(Blackhole bh, HighFrequencyExecutionPlan plan) {
        bh.consume(plan.generator.nextId());
    }

    @Benchmark
    @Fork(value = N_FORKS)
    @Warmup(iterations = N_ITERATIONS, time = N_ITERATIONS)
    @Measurement(iterations = N_ITERATIONS, time = N_ITERATIONS)
    @Threads(4)
    public void measureSingleCall_4Threads(Blackhole bh, HighFrequencyExecutionPlan plan) {
        bh.consume(plan.generator.nextId());
    }

    @Benchmark
    @Fork(value = N_FORKS)
    @Warmup(iterations = N_ITERATIONS, time = N_ITERATIONS)
    @Measurement(iterations = N_ITERATIONS, time = N_ITERATIONS)
    @Threads(12)
    public void measureSingleCall_12Threads(Blackhole bh, HighFrequencyExecutionPlan plan) {
        bh.consume(plan.generator.nextId());
    }

    @State(Scope.Benchmark)
    public static class HighFrequencyExecutionPlan {
        @Param({"Synchronized"})
        public String implementation;
        public FlakeGenerator generator;

        private FlakeGenerator fromImplementation(String implementation) {
            switch (implementation) {
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
