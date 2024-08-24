package com.sb;

import com.sb.flake.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class FlakeGeneratorTestSuite {
    abstract FlakeGenerator makeGenerator(Instant epoch, long machineId);


    @Test
    void nextId_setsBitsCorrectly() {
        final int MACHINE_ID = ThreadLocalRandom.current().nextInt() & ((1 << AtomicSnowflakeGenerator.MACHINE_ID_LENGTH) - 1);
        FlakeGenerator generator = makeGenerator(Instant.now(), MACHINE_ID);

        long snowflake = generator.nextId();

        FlakeData data = generator.parse(snowflake);
        String flakeDefinition = " Snowflake was: " + snowflake + "(" + BinaryUtil.toFormattedBinary(snowflake, AtomicSnowflakeGenerator.RULES) + "), parsed: " + data;
        assertFalse(data.getSinceEpoch().isNegative(), "Negative duration." + flakeDefinition);
        assertTrue(data.getSinceEpoch().toMillis() <= 2, "Weird duration on single call." + flakeDefinition); // Allow 2ms room for very slow computers
        assertEquals(MACHINE_ID, data.getWorkerId(), "Invalid machineId." + flakeDefinition);
        assertEquals(0, data.getSequenceNumber(), "invalid sequence number." + flakeDefinition);
    }

    // Because random testing found an issue when the timestamp started by 1 and was not shifted correctly.
    @Test
    void nextId_setsTimestampCorrectly() throws InterruptedException {
        final int LOW_MACHINE_ID = 2;
        FlakeGenerator generator = makeGenerator(Instant.now(), LOW_MACHINE_ID);
        for (int i = 0; i < 10; i++) {
            Thread.sleep(3);
            long snowflake = generator.nextId();

            FlakeData data = generator.parse(snowflake);
            String flakeDefinition = " Snowflake was: " + snowflake + "(" + BinaryUtil.toFormattedBinary(snowflake, AtomicSnowflakeGenerator.RULES) + "), parsed: " + data;
            assertEquals(LOW_MACHINE_ID, data.getWorkerId(), "Invalid machineId." + flakeDefinition);
        }
    }

    @Test
    void nextId_incrementsSequence() {
        FlakeGenerator generator = makeGenerator(Instant.now(), 1);
        long first = generator.nextId();
        long second = generator.nextId();
        FlakeData firstData = generator.parse(first);
        FlakeData secondData = generator.parse(second);
        assertNotEquals(firstData.getSequenceNumber(), secondData.getSequenceNumber(), "First was: " + firstData + ", second was: " + secondData);
    }

    @Test
    void nextId_timestampIncreasesNaturally() throws InterruptedException {
        FlakeGenerator generator = makeGenerator(Instant.now(), 1);
        long first = generator.nextId();
        Thread.sleep(2);
        long second = generator.nextId();
        FlakeData firstData = generator.parse(first);
        FlakeData secondData = generator.parse(second);
        assertTrue(firstData.getTimestamp().isBefore(secondData.getTimestamp()), "First was: " + firstData + ", second was: " + secondData);
    }

    @Test
    void nextId_whenSingleThreaded_providesUniqueIds() {
        FlakeGenerator generator = makeGenerator(Instant.now(), 1);
        final int N_IDS_TO_GENERATE = 15_000;
        TreeSet<Long> alreadyGenerated = new TreeSet<>();
        for (int i = 0; i < N_IDS_TO_GENERATE; i++) {
            long id = generator.nextId();
            assertTrue(alreadyGenerated.add(id), "Duplicate id: " + id);
        }
    }

    @Test
    void nextId_whenMultithreaded_providesUniqueIds() throws ExecutionException, InterruptedException {
        FlakeGenerator generator = makeGenerator(Instant.now(), 1);

        final int N_LOGICAL_CORES = Runtime.getRuntime().availableProcessors();
        final int N_IDS_TO_GENERATE = 5000;
        Future<Long[]>[] futureResults = new Future[N_LOGICAL_CORES];
        ExecutorService executor = Executors.newFixedThreadPool(N_LOGICAL_CORES);
        for (int i = 0; i < N_LOGICAL_CORES; i++) {
            futureResults[i] = executor.submit(() -> {
                Long[] ids = new Long[N_IDS_TO_GENERATE];
                for (int j = 0; j < N_IDS_TO_GENERATE; j++) {
                    ids[j] = generator.nextId();
                }
                return ids;
            });
        }
        executor.shutdownNow();
        TreeSet<Long> registered = new TreeSet<>();
        for (Future<Long[]> futureResult : futureResults) {
            for (Long id : futureResult.get()) {
                assertTrue(registered.add(id), "Duplicate id: " + id);
            }
        }
    }

    @Test
    void nextId_whenMultithreaded_providesUniqueIds_debug() throws ExecutionException, InterruptedException {
        FlakeGenerator generator = makeGenerator(Instant.now(), 1);

        final int N_LOGICAL_CORES = Runtime.getRuntime().availableProcessors();
        final int N_IDS_TO_GENERATE = 500;
        ConcurrentLinkedQueue<String> debugs = new ConcurrentLinkedQueue<>();
        ArrayList<Callable<Long[]>> callables = new ArrayList<>(N_LOGICAL_CORES);
        for (int i = 0; i < N_LOGICAL_CORES; i++) {
            callables.add(new Caller(i, debugs, N_IDS_TO_GENERATE, generator));
        }
        ExecutorService executor = Executors.newFixedThreadPool(N_LOGICAL_CORES);
        List<Future<Long[]>> futureResults = executor.invokeAll(callables);
        TreeSet<String> sortedEvents = new TreeSet<>(debugs);

        Map<Long, Long> registered = new TreeMap<>();
        for (int thread = 0; thread < futureResults.size(); thread++) {
            Future<Long[]> futureResult = futureResults.get(thread);
            for (Long id : futureResult.get()) {
                Long found = registered.get(id);
                if (found != null) {
                    sortedEvents.forEach(System.out::println);
                    fail("Collision! ID 1 = " + BinaryUtil.toFormattedBinary(found, generator.getRules()) + ", ID 2 = " + BinaryUtil.toFormattedBinary(id, generator.getRules()) + " in thread #" + thread);
                }
                registered.put(id, id);
            }
        }
    }

    private static class Caller implements Callable<Long[]> {
        private final String THREAD_ID;
        private final ConcurrentLinkedQueue<String> ALL_GENERATED;
        private final int N_IDS_TO_GENERATE;
        private final FlakeGenerator generator;

        Caller(int threadId, ConcurrentLinkedQueue<String> allGenerated, int nIdsToGenerate, FlakeGenerator generator) {
            THREAD_ID = String.format("%02d : ", threadId);
            ALL_GENERATED = allGenerated;
            N_IDS_TO_GENERATE = nIdsToGenerate;
            this.generator = generator;
        }

        @Override
        public Long[] call() {
            Long[] ids = new Long[N_IDS_TO_GENERATE];
            for (int j = 0; j < N_IDS_TO_GENERATE; j++) {
                ids[j] = generator.nextId();
                ALL_GENERATED.add(System.nanoTime() + ": " + THREAD_ID + BinaryUtil.toFormattedBinary(ids[j], generator.getRules()));
            }
            return ids;
        }
    }
}
