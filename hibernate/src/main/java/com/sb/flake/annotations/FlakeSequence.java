package com.sb.flake.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

@Retention(RetentionPolicy.RUNTIME)
public @interface FlakeSequence {
    String table() default "";
    GenerationRules rules() default @GenerationRules;
    FlakePreset preset() default FlakePreset.SNOWFLAKE;
}
