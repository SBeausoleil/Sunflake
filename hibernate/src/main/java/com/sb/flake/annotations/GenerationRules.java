package com.sb.flake.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;


@Retention(RetentionPolicy.RUNTIME)
public @interface GenerationRules {
    int NOT_SET = Integer.MIN_VALUE;

    int sequenceSize() default NOT_SET;
    int workerIdSize() default NOT_SET;
    int timestampSize() default NOT_SET;
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
    boolean allowUsageOfSignBit() default false;
    boolean allowTsLooping() default false;
}
