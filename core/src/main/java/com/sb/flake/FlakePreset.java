package com.sb.flake;

import java.time.Instant;
import java.util.function.Function;

/**
 * Preset rules for Sunflake configuration.
 */
public enum FlakePreset {
    SNOWFLAKE(GenerationRules::snowflake),
    SONYFLAKE(GenerationRules::sonyflake),
    VERY_HIGH_FREQUENCY(GenerationRules::veryHighFrequency);

    final Function<Instant, GenerationRules> rulesFunction;

    FlakePreset(Function<Instant, GenerationRules> rulesFunction) {
        this.rulesFunction = rulesFunction;
    }

    public GenerationRules getRules(Instant epoch) {
        return rulesFunction.apply(epoch);
    }
}
