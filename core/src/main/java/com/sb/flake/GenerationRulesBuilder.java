package com.sb.flake;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class GenerationRulesBuilder {
    public static final int DEFAULT_SEQUENCE_SIZE = 12;
    public static final int DEFAULT_WORKER_ID_SIZE = 10;
    public static final int DEFAULT_TIMESTAMP_SIZE = 41;
    public static final boolean DEFAULT_ALLOW_USAGE_OF_SIGN_BIT = false;
    public static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.MILLISECONDS;

    private Integer sequenceSize;
    private Integer workerIdSize;
    private Integer timestampSize;
    private boolean allowUsageOfSignBit = DEFAULT_ALLOW_USAGE_OF_SIGN_BIT;
    private TimeUnit timeUnit = DEFAULT_TIMEUNIT;
    private Integer timeUnitsPerTick;
    private Instant epoch;

    /**
     * Create a builder with the default values, needing only an epoch.
     */
    public GenerationRulesBuilder() {
    }

    /**
     * Create a builder with the default values.
     * @param epoch
     */
    public GenerationRulesBuilder(Instant epoch) {
        this.epoch = epoch;
    }

    /**
     * Create a builder from the given rules.
     * @param rules
     */
    public GenerationRulesBuilder(GenerationRules rules) {
        this.sequenceSize = rules.getSequenceSize();
        this.workerIdSize = rules.getWorkerSize();
        this.timestampSize = rules.getTimestampSize();
        this.allowUsageOfSignBit = rules.canUseSignBit();
        this.timeUnit = rules.getTimeUnit();
        this.timeUnitsPerTick = rules.getTimeUnitsPerTick();
        this.epoch = rules.getEpoch();
    }

    public GenerationRulesBuilder setSequenceSize(int sequenceSize) {
        this.sequenceSize = sequenceSize;
        return this;
    }

    public GenerationRulesBuilder setWorkerIdSize(int workerIdSize) {
        this.workerIdSize = workerIdSize;
        return this;
    }

    public GenerationRulesBuilder setTimestampSize(int timestampSize) {
        this.timestampSize = timestampSize;
        return this;
    }

    public GenerationRulesBuilder setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public GenerationRulesBuilder setAllowUsageOfSignBit(boolean allowUsageOfSignBit) {
        this.allowUsageOfSignBit = allowUsageOfSignBit;
        return this;
    }

    public GenerationRulesBuilder setTimeUnitsPerTick(int timeUnitsPerTick) {
        this.timeUnitsPerTick = timeUnitsPerTick;
        return this;
    }

    public GenerationRulesBuilder setEpoch(Instant epoch) {
        this.epoch = epoch;
        return this;
    }

    public GenerationRules build() {
        if (this.epoch == null) {
            throw new IllegalStateException("The builder requires the epoch be set!");
        }

        int rawSequenceSize;
        int rawWorkerIdSize;
        int rawTimestampSize;

        if (this.sequenceSize != null && this.workerIdSize != null && this.timestampSize != null) {
            rawSequenceSize = this.sequenceSize;
            rawWorkerIdSize = this.workerIdSize;
            rawTimestampSize = this.timestampSize;
        } else if (this.sequenceSize == null && this.workerIdSize == null && timestampSize == null) {
            rawSequenceSize = DEFAULT_SEQUENCE_SIZE;
            rawWorkerIdSize = DEFAULT_WORKER_ID_SIZE;
            rawTimestampSize = DEFAULT_TIMESTAMP_SIZE;
        } else {
            if (this.sequenceSize != null && this.workerIdSize != null) {
                rawSequenceSize = this.sequenceSize;
                rawWorkerIdSize = this.workerIdSize;
                rawTimestampSize = GenerationRules.computeRemainingBits(rawSequenceSize, rawWorkerIdSize, this.allowUsageOfSignBit);
            } else if (this.sequenceSize != null && this.timestampSize != null) {
                rawSequenceSize = this.sequenceSize;
                rawWorkerIdSize = GenerationRules.computeRemainingBits(rawSequenceSize, this.timestampSize, this.allowUsageOfSignBit);
                rawTimestampSize = this.timestampSize;
            } else if (this.workerIdSize != null && this.timestampSize != null) {
                rawSequenceSize = GenerationRules.computeRemainingBits(this.workerIdSize, this.timestampSize, this.allowUsageOfSignBit);
                rawWorkerIdSize = this.workerIdSize;
                rawTimestampSize = this.timestampSize;
            } else {
                throw new IllegalStateException("The builder requires zero, two, or all three sizes set!");
            }
        }


        int rawTimeUnitsPerTick = this.timeUnitsPerTick != null ? this.timeUnitsPerTick : 1;
        return new GenerationRules(rawSequenceSize, rawWorkerIdSize, rawTimestampSize, this.epoch, allowUsageOfSignBit, timeUnit, rawTimeUnitsPerTick);
    }
}