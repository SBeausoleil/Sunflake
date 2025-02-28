package com.sb.flake;

import java.time.Instant;

/**
 * A Flake ID generator that support custom generation rules. This implementation is thread-safe.
 * <p>
 * Internally, all timestamps shared between methods are already masked and shifted.
 * </p>
 */
public class SynchronizedFlakeGenerator extends FlakeGenerator {

    /**
     * Previous timestamp already shifted by 41 bits
     */
    private long previousTimestamp;
    private long sequence;

    public SynchronizedFlakeGenerator(long workerId, GenerationRules rules) {
        super(workerId, rules);
        this.previousTimestamp = this.INSTANCE_START_TIME;
        this.sequence = 0L;
    }

    public synchronized long nextId() {
        long id = shiftedMonotonicTime();
        if (this.previousTimestamp != id) {
            resetSequence(id);
        }
        id = insertSequence(id);
        id |= this.SHIFTED_WORKER_ID;
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
        this.previousTimestamp = newTs;
        this.sequence = 0;
    }
}
