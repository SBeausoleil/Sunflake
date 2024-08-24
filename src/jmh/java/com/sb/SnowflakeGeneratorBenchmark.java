package com.sb;

import com.sb.flake.AtomicSnowflakeGenerator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class SnowflakeGeneratorBenchmark {
    @Benchmark
    public void measureNextId_singleThread(Blackhole bh) {
        AtomicSnowflakeGenerator generator = new AtomicSnowflakeGenerator(1);
        final int N_IDS_TO_GENERATE = 15_000;
        long heap = Long.MIN_VALUE;
        for (int i = 0; i < N_IDS_TO_GENERATE; i++) {
            heap += generator.nextId();
        }
        bh.consume(heap);
    }
}
