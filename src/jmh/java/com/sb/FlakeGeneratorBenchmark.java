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
    public void measureSingleCall(Blackhole bh, HighFrequencyExecutionPlan plan) {
        bh.consume(plan.generator.nextId());
    }

    /*@Benchmark
    public void measureNextId_singleThread(Blackhole bh, HighFrequencyExecutionPlan plan) {
        final int N_IDS_TO_GENERATE = 15_000;
        long heap = Long.MIN_VALUE;
        for (int i = 0; i < N_IDS_TO_GENERATE; i++) {
            heap ^= plan.generator.nextId();
        }
        bh.consume(heap);
    }

    @Benchmark
    public void measureNextId_twoThreads(Blackhole bh) {
        FlakeGenerator generator = makeGenerator(Instant.EPOCH, 0L, GenerationRules.VERY_HIGH_FREQUENCY);
        final int N_IDS_TO_GENERATE = 15_000;
        long heap = Long.MIN_VALUE;
        for (int i = 0; i < N_IDS_TO_GENERATE; i++) {
            heap ^= generator.nextId();
        }
        bh.consume(heap);
    }*/

    @State(Scope.Benchmark)
    public static class HighFrequencyExecutionPlan {
        @Param({"Atomic", "Atoref", "Synchronized"})
        public String implementation;
        public FlakeGenerator generator;

        private FlakeGenerator fromImplementation(String implementation) {
            switch (implementation) {
                case "Atomic":
                    return new AtomicFlakeGenerator(Instant.now(), 1L, GenerationRules.VERY_HIGH_FREQUENCY);
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
