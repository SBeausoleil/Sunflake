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
import java.util.EnumSet;
import java.util.concurrent.ConcurrentHashMap;

public class HibernateFlakeIdGenerator implements AnnotationBasedGenerator<FlakeSequence>, BeforeExecutionGenerator {
    private static final Logger log = LoggerFactory.getLogger(HibernateFlakeIdGenerator.class);

    private static final ConcurrentHashMap<Table, FlakeGenerator> generators = new ConcurrentHashMap<>();

    private FlakeGenerator generator;

    @Override
    public void initialize(FlakeSequence annotation, Member member, GeneratorCreationContext context) {
        this.generator = makeGenerator(context.getPersistentClass().getRootTable());
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

    private static synchronized FlakeGenerator makeGenerator(Table table) {
        return generators.computeIfAbsent(table, c -> new SynchronizedFlakeGenerator(
                SunflakeConfiguration.getEpochProperty(),
                1,
                SunflakeConfiguration.getGlobalRules()
        ));
    }
}
