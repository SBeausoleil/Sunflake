package com.sb;

import java.io.Serializable;

public class GenerationRules implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int SEQUENCE_SIZE;
    private final int WORKER_SIZE;
    private final int TIMESTAMP_SIZE;
    private final boolean ALLOW_USAGE_OF_SIGN_BIT;
    private final boolean ALLOW_TS_LOOPING;

    /**
     * Construct a GenerationRules instance.
     * Rules produced by this constructor do not allow the usage of the sign bit
     * and does not allow timestamp looping.
     * @param sequenceSize how many bits to attribute to the sequence number
     * @param workerSize how many bits to attribute to the worker id (machine ID in Snowflake)
     */
    public GenerationRules(int sequenceSize, int workerSize) {
        this(sequenceSize, workerSize, false, false);
    }

    /**
     * Construct a GenerationRules instance.
     * @param sequenceSize how many bits to attribute to the sequence number
     * @param workerSize how many bits to attribute to the worker id (machine ID in Snowflake)
     * @param allowUsageOfSignBit if the sign bit may be used.
     */
    public GenerationRules(int sequenceSize, int workerSize, boolean allowUsageOfSignBit, boolean allowTsLooping) {
        this(sequenceSize,
                workerSize,
                computeRemainingBits(sequenceSize, workerSize, allowUsageOfSignBit),
                allowUsageOfSignBit,
                allowTsLooping);
    }

    public GenerationRules(int sequenceSize, int workerSize, int timestampSize, boolean allowUsageOfSignBit, boolean allowTsLooping) {
        this.SEQUENCE_SIZE = sequenceSize;
        this.WORKER_SIZE = workerSize;
        this.TIMESTAMP_SIZE = timestampSize;
        this.ALLOW_USAGE_OF_SIGN_BIT = allowUsageOfSignBit;
        this.ALLOW_TS_LOOPING = allowTsLooping;

        int totalBits = SEQUENCE_SIZE + WORKER_SIZE + TIMESTAMP_SIZE;
        if ((allowUsageOfSignBit && totalBits > Long.SIZE)
        || (!allowUsageOfSignBit && totalBits >= Long.SIZE)) {
            throw new IllegalArgumentException("Total of bits is larger than 64 or would use the sign bit without being allowed!");
        }
    }

    public int getSequenceSize() {
        return SEQUENCE_SIZE;
    }

    public int getWorkerSize() {
        return WORKER_SIZE;
    }

    public boolean canUseSignBit() {
        return ALLOW_USAGE_OF_SIGN_BIT;
    }

    public boolean canLoopTimestamp() {
        return ALLOW_TS_LOOPING;
    }

    public static int computeRemainingBits(int sequenceSize, int workerSize, boolean allowUsageOfSignBit) {
        return Long.SIZE - sequenceSize - workerSize - (allowUsageOfSignBit ? 0 : 1);
    }
}
