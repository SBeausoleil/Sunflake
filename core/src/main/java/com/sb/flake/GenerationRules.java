package com.sb.flake;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class GenerationRules implements Serializable {
    protected static final long serialVersionUID = 1L;

    public static final GenerationRules SNOWFLAKE = new GenerationRules(12, 10, 41, false, TimeUnit.MILLISECONDS);
    public static final GenerationRules SONYFLAKE = new GenerationRules(8, 16, 39, false, TimeUnit.MILLISECONDS);
    public static final GenerationRules VERY_HIGH_FREQUENCY = new GenerationRulesBuilder()
            .setSequenceSize(32)
            .setWorkerIdSize(1)
            .createGenerationRules();

    protected final int SEQUENCE_SIZE;
    protected final int WORKER_ID_SIZE;
    protected final int TIMESTAMP_SIZE;
    protected final TimeUnit TIME_UNIT;
    
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

    /**
     * Construct a GenerationRules instance.
     * Rules produced by this constructor do not allow the usage of the sign bit
     * and does not allow timestamp looping.
     * @param sequenceSize how many bits to attribute to the sequence number
     * @param workerIdSize how many bits to attribute to the worker id (machine ID in Snowflake)
     */
    public GenerationRules(int sequenceSize, int workerIdSize) {
        this(sequenceSize, workerIdSize, false);
    }

    /**
     * Construct a GenerationRules instance.
     * @param sequenceSize how many bits to attribute to the sequence number
     * @param workerIdSize how many bits to attribute to the worker id (machine ID in Snowflake)
     * @param allowUsageOfSignBit if the sign bit may be used.
     */
    public GenerationRules(int sequenceSize, int workerIdSize, boolean allowUsageOfSignBit) {
        this(sequenceSize,
                workerIdSize,
                computeRemainingBits(sequenceSize, workerIdSize, allowUsageOfSignBit),
                allowUsageOfSignBit,
                TimeUnit.MILLISECONDS);
    }
    
    public GenerationRules(int sequenceSize, int workerIdSize, int timestampSize, boolean allowUsageOfSignBit, TimeUnit timeUnit) {
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

    public static int computeRemainingBits(int size1, int size2, boolean allowUsageOfSignBit) {
        return Long.SIZE - size1 - size2 - (allowUsageOfSignBit ? 0 : 1);
    }
}
