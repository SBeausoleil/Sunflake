package com.sb.flake;

import java.util.concurrent.TimeUnit;

public class GenerationRulesBuilder {
    public static final int DEFAULT_SEQUENCE_SIZE = 12;
    public static final int DEFAULT_WORKER_ID_SIZE = 10;
    public static final int DEFAULT_TIMESTAMP_SIZE = 41;

    private Integer sequenceSize;
    private Integer workerIdSize;
    private boolean allowUsageOfSignBit = false;
    private Integer timestampSize;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

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

    public GenerationRules createGenerationRules() {
        int rawSequenceSize, rawWorkerIdSize, rawTimestampSize;
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
        return new GenerationRules(rawSequenceSize, rawWorkerIdSize, rawTimestampSize, allowUsageOfSignBit, timeUnit);
    }
}