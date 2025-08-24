package com.sb.flake;

public final class SunflakeConstants {
    private SunflakeConstants() {
    }

    public static final String PERSISTENCE_UNIT_NAME = "sunflake.hibernate";
    public static final String KEEP_ALIVE_TABLE_NAME = "sunflake_alive_markers";
}
