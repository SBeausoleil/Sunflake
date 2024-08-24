package com.sb.flake;

public final class TimeSequence implements Cloneable{
    /**
     * Last shifted and masked timestamp
     */
    public long lastTimestamp;
    public long sequence;

    public TimeSequence(long timestamp, long sequence) {
        this.lastTimestamp = timestamp;
        this.sequence = sequence;
    }

    @Override
    public TimeSequence clone() {
        try {
            return (TimeSequence) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("TimeSequence cloning failed");
        }
    }
}
