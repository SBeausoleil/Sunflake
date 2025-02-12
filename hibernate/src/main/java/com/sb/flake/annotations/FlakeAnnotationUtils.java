package com.sb.flake.annotations;

import com.sb.flake.GenerationRules;
import com.sb.flake.GenerationRulesBuilder;

import static com.sb.flake.GenerationRulesBuilder.*;
import static com.sb.flake.annotations.GenerationRules.NOT_SET;

public final class FlakeAnnotationUtils {
    private FlakeAnnotationUtils() {}

    public static GenerationRules readRules(FlakeSequence annotation) {
        com.sb.flake.annotations.GenerationRules rules = annotation.rules();
        if (rules.sequenceSize() == NOT_SET
                && rules.workerIdSize() == NOT_SET
                && rules.timestampSize() == NOT_SET) {
            return annotation.preset().getRules();
        } else {
            return new GenerationRulesBuilder()
                    .setSequenceSize(rules.sequenceSize() != NOT_SET ? rules.sequenceSize() : DEFAULT_SEQUENCE_SIZE)
                    .setWorkerIdSize(rules.workerIdSize() != NOT_SET ? rules.workerIdSize() : DEFAULT_WORKER_ID_SIZE)
                    .setTimestampSize(rules.timestampSize() != NOT_SET ? rules.timestampSize() : DEFAULT_TIMESTAMP_SIZE)
                    .setAllowUsageOfSignBit(rules.allowUsageOfSignBit())
                    .setTimeUnit(rules.timeUnit())
                    .build();
        }
    }
}
