package com.sb.flake;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A Flake ID generator that support custom generation rules. This implementation is thread-safe.
 * <p>
 *     Internally, all timestamps shared between methods are already masked and shifted.
 * </p>
 */
public class SynchronizedFlakeGenerator extends FlakeGenerator {

    protected final long SHIFTED_MACHINE_ID;
    /**
     * Time in the time unit of this generator since the real epoch when this generator was instantiated.
     */
    private final long INSTANCE_START_TIME;
    /**
     * To avoid issues with leap seconds and backward flowing time,
     * this generator uses a monotonical clock.
     * Since the relation between the epoch of the clock and wall-time is unknown,
     * we maintain an internal start time of the clock to allow later establishing
     * of the actual real timestamp with the following calculation:
     * <p>
     *     <code>
     *         toTimeUnit(CLOCK_TIME - CLOCK_EPOCH) + INSTANCE_START_TIME = TIMESTAMP
     *     </code>
     * </p>
     */
    private final long CLOCK_EPOCH;
    /**
     * Previous timestamp already shifted by 41 bits
     */
    private long previousTimestamp;
    private long sequence;

    public SynchronizedFlakeGenerator(Instant epoch, long workerId, GenerationRules rules) {
        super(epoch, rules);
        long maskedId = workerId & rules.getWorkerIdMask();
        if (maskedId != workerId) {
            throw new IllegalArgumentException("Invalid workerId: " + workerId + " (too big). " +
                    "WorkerId must be a " + rules.getWorkerSize() + " bits integer.");
        }

        this.SHIFTED_MACHINE_ID = maskedId << rules.getWorkerIdShift();
        long msSinceEpoch = System.currentTimeMillis() - epoch.toEpochMilli();
        this.INSTANCE_START_TIME = rules.getTimeUnit().convert(msSinceEpoch, TimeUnit.MILLISECONDS);
        this.CLOCK_EPOCH = System.nanoTime();

        this.previousTimestamp = this.INSTANCE_START_TIME;
        this.sequence = 0L;
    }

    public synchronized long nextId() {
        // TODO detect time loop
        long id = shiftedMonotonicTime();
        if (this.previousTimestamp != id) {
            resetSequence(id);
        }
        id = insertSequence(id);
        id |= this.SHIFTED_MACHINE_ID;
        return id;
    }

    private long insertSequence(long id) {
        long sequenceNumber = sequence++;
        long maskedSequenceNumber = sequenceNumber & this.RULES.SEQUENCE_MASK;
        /* If the maskedSequenceNumber is smaller than the original sequence number,
        * it means that the sequence number is larger than the max possible sequence number for the rules of the generator
        * Loop instead of simple condition in case the queue to get a sequence number at the next timestamp
        * was larger than the max possible sequence number for the rules of the generator.
         */
        while (maskedSequenceNumber != sequenceNumber) {
            id = awaitNextTimestamp(id);
            sequenceNumber = sequence++;
            maskedSequenceNumber = sequenceNumber & this.RULES.SEQUENCE_MASK;
        }
        id |= maskedSequenceNumber;
        return id;
    }

    private long shiftedMonotonicTime() {
        //System.out.println();
        long ts = System.nanoTime() - CLOCK_EPOCH;
        ts = this.RULES.getTimeUnit().convert(ts, TimeUnit.NANOSECONDS);
        //System.out.println("TS: " + ts + " (" + toUnformattedBinary(ts) + ")");
        ts += INSTANCE_START_TIME;
        //System.out.println("Adjusted ts: " + ts + " (" + toUnformattedBinary(ts) + ")");
        ts <<= this.RULES.TIMESTAMP_SHIFT;
        //System.out.println("Shifted (" + this.RULES.TIMESTAMP_SHIFT + ") ts: " + ts + " (" + toUnformattedBinary(ts) + ")");
        //System.out.println(toFormattedBinary(ts, this.RULES));
        ts &= this.RULES.SIGN_MASK;
        return ts;
    }

    private long awaitNextTimestamp(long tsOnArrival) {
        long ts;
        do {
            Thread.onSpinWait(); // Free some CPU resources
            ts = shiftedMonotonicTime();
        } while (ts <= tsOnArrival);
        resetSequence(ts);
        return ts;
    }

    private void resetSequence(long newTs) {
        if (newTs > this.previousTimestamp) {
            this.previousTimestamp = newTs;
            this.sequence = 0;
        }
    }
}
