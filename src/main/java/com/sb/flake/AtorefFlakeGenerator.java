package com.sb.flake;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Flake ID generator that support custom generation rules.
 * <p>
 *     Internally, all timestamps shared between methods are already masked and shifted.
 * </p>
 */
public class AtorefFlakeGenerator extends FlakeGenerator {
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
    private final AtomicReference<TimeSequence> sharedTimeSequence;

    public AtorefFlakeGenerator(Instant epoch, long workerId, GenerationRules rules) {
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

        this.sharedTimeSequence = new AtomicReference<>(new TimeSequence(INSTANCE_START_TIME, 0));
    }

    public long nextId() {
        TimeSequence localTimeSequence = acquireAndMaybeReset(shiftedMonotonicTime());
        long id = localTimeSequence.lastTimestamp;
        id = insertSequence(id, localTimeSequence);
        id |= this.SHIFTED_MACHINE_ID;
        return id;
    }

    private long insertSequence(long id, TimeSequence localTimeSequence) {
        long sequenceNumber = localTimeSequence.sequence;
        long maskedSequenceNumber = sequenceNumber & this.RULES.SEQUENCE_MASK;
        /* If the maskedSequenceNumber is smaller than the original sequence number,
        * it means that the sequence number is larger than the max possible sequence number for the rules of the generator
        * Loop instead of simple condition in case the queue to get a sequence number at the next timestamp
        * was larger than the max possible sequence number for the rules of the generator.
         */
        while (maskedSequenceNumber != sequenceNumber) {
            localTimeSequence = awaitNextTimestampAndAcquireLocalCopy(id);
            id = localTimeSequence.lastTimestamp;
            sequenceNumber = localTimeSequence.sequence;
            maskedSequenceNumber = sequenceNumber & this.RULES.SEQUENCE_MASK;
        }
        id |= maskedSequenceNumber;
        return id;
    }

    private long shiftedMonotonicTime() {
        //System.out.println();
        // Maybe getAndAquire? getAndUpdate tries to avoid contention, maybe leading to shared sequence...
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

    private TimeSequence awaitNextTimestampAndAcquireLocalCopy(long tsOnArrival) {
        long ts;
        do {
            Thread.onSpinWait(); // Free some CPU resources
            ts = shiftedMonotonicTime();
        } while (ts <= tsOnArrival);
        return acquireAndMaybeReset(ts);
    }

    // There might be contentions here...
    private synchronized TimeSequence acquireAndMaybeReset(long newTs) {
        TimeSequence shared = this.sharedTimeSequence.getAcquire();
        TimeSequence toReturn = shared.clone();
        if (newTs > shared.lastTimestamp) {
            shared.lastTimestamp = newTs;
            shared.sequence = 1; // Reserve sequence 0 for ourselves
            this.sharedTimeSequence.setRelease(shared);

            toReturn.lastTimestamp = newTs;
            toReturn.sequence = 0;
        } else {
            shared.sequence++;
            this.sharedTimeSequence.setRelease(shared);
        }
        return toReturn;
    }

    /**
     * Parse a flake ID as if it were generated by this generator instance.
     * @param flake the flake id to parse
     * @return the components of that snowflake.
     */
    public FlakeData parse(long flake) {
        long msSinceEpoch = flake >> this.RULES.TIMESTAMP_SHIFT & this.RULES.TIMESTAMP_MASK;
        Instant timestamp = this.EPOCH.plusMillis(msSinceEpoch);
        short workerId = (short) (flake >> this.RULES.getWorkerIdShift() & this.RULES.WORKER_ID_MASK);
        short sequenceNumber = (short) (flake & this.RULES.SEQUENCE_MASK);
        return new FlakeData(flake, timestamp, Duration.ofMillis(msSinceEpoch), workerId, sequenceNumber);
    }

    /**
     * Isolate the components of the flake as if it were generated by this generator instance.
     * @param flake the flake id to parse
     * @return a long array where [0] is the time difference since the epoch, [1] the workerId, [2] the sequence number.
     */
    public long[] primitiveParse(long flake) {
        return new long[] {
                flake >> this.RULES.TIMESTAMP_SHIFT & this.RULES.TIMESTAMP_MASK,
                flake >> this.RULES.getWorkerIdShift() & this.RULES.WORKER_ID_MASK,
                flake & this.RULES.SEQUENCE_MASK
        };
    }

    public GenerationRules getRules() {
        return this.RULES;
    }
}
