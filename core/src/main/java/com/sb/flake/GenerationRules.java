package com.sb.flake;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Rules that a flake generator must follow.
 */
public class GenerationRules implements Serializable {
    private static final long serialVersionUID = 1L;

    protected final int SEQUENCE_SIZE;
    protected final int WORKER_ID_SIZE;
    protected final int TIMESTAMP_SIZE;
    protected final TimeUnit TIME_UNIT;
    protected final int TIME_UNITS_PER_TICK;
    
    protected final boolean ALLOW_USAGE_OF_SIGN_BIT;
    /**
     * Mask that when used with the AND operator on an ID
     * will either preserve (ALLOW_USAGE_OF_SIGN_BIT = true) or
     * set to zero (ALLOW_USAGE_OF_SIGN_BIT = false) the most significant bit.
     */
    protected final long SIGN_MASK;

    protected final long SEQUENCE_MASK;
    protected final long WORKER_ID_MASK;
    protected final long TIMESTAMP_MASK;
    protected final int TIMESTAMP_SHIFT;

    protected final long SHIFTED_WORKER_ID_MASK;
    protected final long SHIFTED_TIMESTAMP_MASK;

    protected final Instant EPOCH;

    /**
     * Construct a GenerationRules instance.
     * Rules produced by this constructor do not allow the usage of the sign bit
     * and does not allow timestamp looping.
     * @param sequenceSize how many bits to attribute to the sequence number
     * @param workerIdSize how many bits to attribute to the worker id (machine ID in Snowflake)
     * @param epoch the epoch to use
     */
    public GenerationRules(int sequenceSize, int workerIdSize, Instant epoch) {
        this(sequenceSize, workerIdSize, epoch, false);
    }

    /**
     * Construct a GenerationRules instance.
     * @param sequenceSize how many bits to attribute to the sequence number
     * @param workerIdSize how many bits to attribute to the worker id (machine ID in Snowflake)
     * @param allowUsageOfSignBit if the sign bit may be used.
     */
    public GenerationRules(int sequenceSize, int workerIdSize, Instant epoch, boolean allowUsageOfSignBit) {
        this(sequenceSize,
                workerIdSize,
                computeRemainingBits(sequenceSize, workerIdSize, allowUsageOfSignBit),
                epoch,
                allowUsageOfSignBit,
                TimeUnit.MILLISECONDS, 1);
    }

    /**
     * Construct a GenerationRules instance.
     * @param sequenceSize how many bits to attribute to the sequence number
     * @param workerIdSize how many bits to attribute to the worker id (machine ID in Snowflake)
     * @param timestampSize how many bits to attribute to the timestamp
     * @param epoch from when are the ticks counting from
     * @param allowUsageOfSignBit if the sign bit may be used for the timestamp
     * @param timeUnit unit of time used to count timestamp increases
     * @param timeUnitsPerTick how many time units are in a tick
     */
    public GenerationRules(int sequenceSize, int workerIdSize, int timestampSize, Instant epoch, boolean allowUsageOfSignBit, TimeUnit timeUnit, int timeUnitsPerTick) {
        this.SEQUENCE_SIZE = sequenceSize;
        this.WORKER_ID_SIZE = workerIdSize;
        this.TIMESTAMP_SIZE = timestampSize;
        this.ALLOW_USAGE_OF_SIGN_BIT = allowUsageOfSignBit;
        if (allowUsageOfSignBit) {
            this.SIGN_MASK = 0xFF_FF_FF_FF_FF_FF_FF_FFL;
        } else {
            this.SIGN_MASK = ~(1L << 63);
        }
        this.TIME_UNIT = timeUnit;
        this.TIME_UNITS_PER_TICK = timeUnitsPerTick;

        int totalBits = SEQUENCE_SIZE + WORKER_ID_SIZE + TIMESTAMP_SIZE;
        if ((allowUsageOfSignBit && totalBits > Long.SIZE)
        || (!allowUsageOfSignBit && totalBits >= Long.SIZE)) {
            throw new IllegalArgumentException("Total of bits is larger than 64 or would use the sign bit without being allowed!");
        }

        this.SEQUENCE_MASK = (1L << SEQUENCE_SIZE) - 1;
        this.WORKER_ID_MASK = (1L << WORKER_ID_SIZE) - 1;
        this.TIMESTAMP_MASK = (1L << TIMESTAMP_SIZE) - 1;

        this.TIMESTAMP_SHIFT = SEQUENCE_SIZE + WORKER_ID_SIZE;

        this.SHIFTED_WORKER_ID_MASK = WORKER_ID_MASK << SEQUENCE_SIZE;
        this.SHIFTED_TIMESTAMP_MASK = TIMESTAMP_MASK << TIMESTAMP_SHIFT;

        this.EPOCH = epoch;
    }

    /**
     * Generation rules suggested by Twitter.
     * <ul>
     *     <li>41 bits for the timestamp in milliseconds since the epoch</li>
     *     <li>10 bits for the worker id</li>
     *     <li>12 bits for the sequence number</li>
     * </ul>
     * @param epoch
     * @return
     */
    public static GenerationRules snowflake(Instant epoch) {
        return new GenerationRulesBuilder()
                .setTimestampSize(41)
                .setWorkerIdSize(10)
                .setSequenceSize(12)
                .setEpoch(epoch)
                .setTimeUnit(TimeUnit.MILLISECONDS)
                .setTimeUnitsPerTick(1)
                .setAllowUsageOfSignBit(false)
                .build();
    }

    /**
     * Generation rules suggested by Sony.
     * <ul>
     *     <li>8 bits for the sequence number</li>
     *     <li>16 bits for the worker id</li>
     *     <li>39 bits for the timestamp in ticks of 10 ms</li>
     * </ul>
     */
    public static GenerationRules sonyflake(Instant epoch) {
        return new GenerationRulesBuilder()
                .setTimestampSize(39)
                .setWorkerIdSize(16)
                .setSequenceSize(8)
                .setEpoch(epoch)
                .setTimeUnit(TimeUnit.MILLISECONDS)
                .setTimeUnitsPerTick(10)
                .setAllowUsageOfSignBit(false)
                .build();
    }

    /**
     * Generation rules tolerating a single worker but with up to 2^32 new entries per millisecond.
     * Mostly used for tests.
     */
    public static GenerationRules veryHighFrequency(Instant epoch) {
        return new GenerationRulesBuilder()
                .setTimestampSize(31)
                .setWorkerIdSize(1)
                .setSequenceSize(32)
                .setEpoch(epoch)
                .setTimeUnit(TimeUnit.MILLISECONDS)
                .setTimeUnitsPerTick(1)
                .setAllowUsageOfSignBit(true)
                .build();
    }

    public int getSequenceSize() {
        return SEQUENCE_SIZE;
    }

    public long getSequenceMask() {
        return SEQUENCE_MASK;
    }

    public int getWorkerSize() {
        return WORKER_ID_SIZE;
    }

    public long getWorkerIdMask() {
        return WORKER_ID_MASK;
    }

    public int getWorkerIdShift() {
        return SEQUENCE_SIZE;
    }

    public long getShiftedWorkerIdMask() {
        return SHIFTED_WORKER_ID_MASK;
    }

    public int getTimestampSize() {
        return TIMESTAMP_SIZE;
    }

    public long getTimestampMask() {
        return TIMESTAMP_MASK;
    }

    public int getTimestampShift() {
        return TIMESTAMP_SHIFT;
    }

    public long getShiftedTimestampMask() {
        return SHIFTED_TIMESTAMP_MASK;
    }

    public boolean canUseSignBit() {
        return ALLOW_USAGE_OF_SIGN_BIT;
    }

    public TimeUnit getTimeUnit() {
        return TIME_UNIT;
    }

    public Instant getEpoch() {
        return EPOCH;
    }

    public int getTimeUnitsPerTick() {
        return TIME_UNITS_PER_TICK;
    }

    /**
     * Compute how many bits are left in a 64 bits integer after taking into account the sizes of the other two components.
     * @param size1 size of the first component
     * @param size2 size of the second component
     * @param allowUsageOfSignBit if the sign bit may be used
     * @return the number of bits left for the third component
     */
    protected static int computeRemainingBits(int size1, int size2, boolean allowUsageOfSignBit) {
        return Long.SIZE - size1 - size2 - (allowUsageOfSignBit ? 0 : 1);
    }

    /**
     * Parse a flake ID as if it were generated by this generator instance.
     * @param flake the flake id to parse
     * @return the components of that snowflake.
     */
    public FlakeData parse(long flake) {
        long[] components = isolateComponents(flake);
        // Convert a tick to microseconds since epoch
        long sinceEpoch = components[0];
        sinceEpoch *= this.TIME_UNITS_PER_TICK; // Decompress if there were multiple units per tick
        sinceEpoch = TimeUnit.MICROSECONDS.convert(sinceEpoch, TIME_UNIT);
        Instant timestamp = this.EPOCH.plus(sinceEpoch, ChronoUnit.MICROS);
        long workerId = components[1];
        long sequenceNumber = components[2];
        return new FlakeData(flake, timestamp, Duration.between(EPOCH, timestamp), workerId, sequenceNumber);
    }

    /**
     * Isolate the components of the flake as if it were generated by this rules set.
     *
     * @param flake the flake id to parse
     * @return a long array where [0] is the time difference since the epoch, [1] the workerId, [2] the sequence number.
     */
    public long[] isolateComponents(long flake) {
        return new long[] {
                flake >> this.TIMESTAMP_SHIFT & this.TIMESTAMP_MASK,
                flake >> this.getWorkerIdShift() & this.WORKER_ID_MASK,
                flake & this.SEQUENCE_MASK
        };
    }

    /**
     * Mask the worker ID to fit in the allowed number of bits and shift it to its position.
     * @param workerId the worker ID to mask and shift
     * @return the masked and shifted worker ID
     * @throws IllegalArgumentException if the worker ID is too big to fit in the allowed number of bits
     */
    public long maskAndShiftWorkerId(long workerId) {
        long maskedId = workerId & WORKER_ID_MASK;
        if (maskedId != workerId) {
            throw new IllegalArgumentException("Invalid workerId: " + workerId + " (too big). " +
                    "WorkerId must be a " + WORKER_ID_SIZE + " bits integer.");
        }
        return maskedId << getWorkerIdShift();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GenerationRules that = (GenerationRules) o;
        return SEQUENCE_SIZE == that.SEQUENCE_SIZE
                && WORKER_ID_SIZE == that.WORKER_ID_SIZE
                && TIMESTAMP_SIZE == that.TIMESTAMP_SIZE
                && TIME_UNITS_PER_TICK == that.TIME_UNITS_PER_TICK
                && ALLOW_USAGE_OF_SIGN_BIT == that.ALLOW_USAGE_OF_SIGN_BIT
                && SIGN_MASK == that.SIGN_MASK
                && SEQUENCE_MASK == that.SEQUENCE_MASK
                && WORKER_ID_MASK == that.WORKER_ID_MASK
                && TIMESTAMP_MASK == that.TIMESTAMP_MASK
                && TIMESTAMP_SHIFT == that.TIMESTAMP_SHIFT
                && SHIFTED_WORKER_ID_MASK == that.SHIFTED_WORKER_ID_MASK
                && SHIFTED_TIMESTAMP_MASK == that.SHIFTED_TIMESTAMP_MASK
                && TIME_UNIT == that.TIME_UNIT
                && Objects.equals(EPOCH, that.EPOCH);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SEQUENCE_SIZE, WORKER_ID_SIZE, TIMESTAMP_SIZE,
                TIME_UNIT, TIME_UNITS_PER_TICK, ALLOW_USAGE_OF_SIGN_BIT, SIGN_MASK,
                SEQUENCE_MASK, WORKER_ID_MASK, TIMESTAMP_MASK, TIMESTAMP_SHIFT,
                SHIFTED_WORKER_ID_MASK, SHIFTED_TIMESTAMP_MASK, EPOCH);
    }
}
