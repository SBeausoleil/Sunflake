package com.sb.flake;

public final class SunflakeConstants {
    private SunflakeConstants() {
    }

    /**
     * The name of the table used to keep track of alive markers.
     */
    // Must be in UPPERCASE to avoid issues with some databases (e.g. H2 in our tests)
    public static final String KEEP_ALIVE_TABLE_NAME = "SUNFLAKE_ALIVE_MARKER";
}
