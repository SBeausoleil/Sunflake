package com.sb.flake;

import java.io.Serializable;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public final class Snowflake implements Serializable {
    private static final long serialVersionUID = 1L;


    public static final Instant DEFAULT_EPOCH = Instant.parse("2015-01-01T00:00:00Z");

    /**
     * Number of bits in the sequence.
     */
    public static final int SEQUENCE_LENGTH = 12;
    public static final int SEQUENCE_MASK = (1 << SEQUENCE_LENGTH) - 1;
    public static final int MACHINE_ID_LENGTH = 10;
    public static final int MACHINE_ID_SHIFT = SEQUENCE_LENGTH;
    /**
     * Mask on the machine id to ensure correct size.
     * Apply on non-shifted number.
     */
    public static final int MACHINE_ID_MASK = ((1 << (MACHINE_ID_LENGTH)) - 1);
    public static final int TS_LENGTH = 41;
    public static final int TS_SHIFT = SEQUENCE_LENGTH + MACHINE_ID_LENGTH;

    private Snowflake() {}

    public static final GenerationRules RULES = new GenerationRules(SEQUENCE_LENGTH, MACHINE_ID_LENGTH, TS_LENGTH, false, false, TimeUnit.MILLISECONDS);
}
