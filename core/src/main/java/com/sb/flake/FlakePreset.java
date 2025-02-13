package com.sb.flake;

public enum FlakePreset {
    SNOWFLAKE(GenerationRules.SNOWFLAKE),
    SONYFLAKE(GenerationRules.SONYFLAKE),
    VERY_HIGH_FREQUENCY(GenerationRules.VERY_HIGH_FREQUENCY);

    final GenerationRules rules;

    FlakePreset(GenerationRules rules) {
        this.rules = rules;
    }

    public GenerationRules getRules() {
        return rules;
    }
}
