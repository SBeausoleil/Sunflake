package com.sb.flake;

import com.sb.flake.annotations.FlakeSequence;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.AnnotationBasedGenerator;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.mapping.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Member;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;

public class HibernateFlakeIdGenerator implements AnnotationBasedGenerator<FlakeSequence>, BeforeExecutionGenerator {
    private static final Logger log = LoggerFactory.getLogger(HibernateFlakeIdGenerator.class);

    private static final ConcurrentHashMap<Table, FlakeGenerator> generators = new ConcurrentHashMap<>();

    private FlakeGenerator generator;

    @Override
    public void initialize(FlakeSequence annotation, Member member, GeneratorCreationContext context) {
        // Read https://docs.jboss.org/hibernate/orm/6.5/javadocs/org/hibernate/generator/AnnotationBasedGenerator.html#initialize(A,java.lang.reflect.Member,org.hibernate.generator.GeneratorCreationContext)
        this.generator = makeGenerator(context.getPersistentClass().getRootTable(), annotation);
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        log.debug("Generate: FlakeIdGenerator: {}, generating ID for: {} ({})",
                System.identityHashCode(this), owner, System.identityHashCode(owner));
        return generator.nextId();
    }

    private static synchronized FlakeGenerator makeGenerator(Table table, FlakeSequence annotation) {
        return generators.computeIfAbsent(table, c -> new SynchronizedFlakeGenerator(
                LocalDate.of(2025, 1, 1).atStartOfDay().toInstant(ZoneOffset.UTC),
                1,
                annotation.preset().getRules()
        ));
    }
}
