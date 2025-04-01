package com.sb.flake;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public abstract class FlakeGenerator implements Serializable {
    protected final GenerationRules RULES;
    protected final long SHIFTED_WORKER_ID;
    /**
     * Time in the time unit of this generator since the real epoch when this generator was instantiated.
     */
    protected final long INSTANCE_START_TIME;
    /**
     * To avoid issues with leap seconds and backward flowing time,
     * this generator uses a monotonical clock.
     * Since the relation between the epoch of the clock and wall-time is unknown,
     * we maintain an internal start time of the clock to allow later establishing
     * of the actual real timestamp with the following calculation:
     * <p>
     * <code>
     *  toTimeUnit(CLOCK_TIME - CLOCK_EPOCH) + INSTANCE_START_TIME = TIMESTAMP
     * </code>
     * </p>
     */
    protected final long CLOCK_EPOCH;

    protected FlakeGenerator(long workerId, GenerationRules rules) {
        this.RULES = rules;

        long maskedId = workerId & rules.getWorkerIdMask();
        if (maskedId != workerId) {
            throw new IllegalArgumentException("Invalid workerId: " + workerId + " (too big). " +
                    "WorkerId must be a " + rules.getWorkerSize() + " bits integer.");
        }
        this.SHIFTED_WORKER_ID = maskedId << rules.getWorkerIdShift();

        long msSinceEpoch = System.currentTimeMillis() - rules.getEpoch().toEpochMilli();
        this.INSTANCE_START_TIME = rules.getTimeUnit().convert(msSinceEpoch, TimeUnit.MILLISECONDS) / rules.getTimeUnitsPerTick();
        this.CLOCK_EPOCH = System.nanoTime();
    }

    public GenerationRules getRules() {
        return RULES;
    }

    /**
     * Get the current timestamp in the time unit of this generator.
     * The timestamp is already shifted to the correct location.
     * @return the current timestamp
     */
    protected long shiftedMonotonicTime() {
        long ts = System.nanoTime() - CLOCK_EPOCH;
        ts = this.RULES.getTimeUnit().convert(ts, TimeUnit.NANOSECONDS);
        ts /= this.RULES.TIME_UNITS_PER_TICK;
        ts += INSTANCE_START_TIME;
        ts <<= this.RULES.TIMESTAMP_SHIFT;
        ts &= this.RULES.SIGN_MASK;
        return ts;
    }

    public abstract long nextId();

}
