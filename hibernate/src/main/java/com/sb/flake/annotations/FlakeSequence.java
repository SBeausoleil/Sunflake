package com.sb.flake.annotations;

import com.sb.flake.HibernateFlakeIdGenerator;
import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

@IdGeneratorType(HibernateFlakeIdGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD})
public @interface FlakeSequence {
    String table() default "";
    GenerationRules rules() default @GenerationRules;
    FlakePreset preset() default FlakePreset.SNOWFLAKE;
}
