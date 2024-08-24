package com.sb.flake;

import java.time.Duration;
import java.time.Instant;

public class FlakeData {
    private final long FLAKE;
    private final Instant TIMESTAMP;
    private final Duration SINCE_EPOCH;
    private final long workerId;
    private final long SEQUENCE_NUMBER;

    public FlakeData(long flake, Instant timestamp, Duration sinceEpoch, long workerId, long SEQUENCE_NUMBER) {
        this.FLAKE = flake;
        this.TIMESTAMP = timestamp;
        this.SINCE_EPOCH = sinceEpoch;
        this.workerId = workerId;
        this.SEQUENCE_NUMBER = SEQUENCE_NUMBER;
    }

    public long getFlake() {
        return FLAKE;
    }

    public Instant getTimestamp() {
        return TIMESTAMP;
    }

    public Duration getSinceEpoch() {
        return SINCE_EPOCH;
    }

    public long getWorkerId() {
        return workerId;
    }

    public long getSequenceNumber() {
        return SEQUENCE_NUMBER;
    }

    @Override
    public String toString() {
        return "FlakeData{" +
                "timestamp=" + TIMESTAMP +
                ", sinceEpoch=" + SINCE_EPOCH +
                ", workerId=" + workerId +
                ", sequenceNumber=" + SEQUENCE_NUMBER +
                '}';
    }
}
