package com.sb;

import java.time.Duration;
import java.time.Instant;

public class FlakeData {
    private final long FLAKE;
    private final Instant TIMESTAMP;
    private final Duration SINCE_EPOCH;
    private final short MACHINE_ID;
    private final short SEQUENCE_NUMBER;

    public FlakeData(long flake, Instant TIMESTAMP, Duration sinceEpoch, short MACHINE_ID, short SEQUENCE_NUMBER) {
        this.FLAKE = flake;
        this.TIMESTAMP = TIMESTAMP;
        this.SINCE_EPOCH = sinceEpoch;
        this.MACHINE_ID = MACHINE_ID;
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

    public short getMachineId() {
        return MACHINE_ID;
    }

    public short getSequenceNumber() {
        return SEQUENCE_NUMBER;
    }

    @Override
    public String toString() {
        return "FlakeData{" +
                "timestamp=" + TIMESTAMP +
                ", sinceEpoch=" + SINCE_EPOCH +
                ", machineId=" + MACHINE_ID +
                ", sequenceNumber=" + SEQUENCE_NUMBER +
                '}';
    }
}
