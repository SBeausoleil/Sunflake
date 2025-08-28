package com.sb.flake;

import com.sb.flake.annotations.FlakeSequence;
import jakarta.persistence.PersistenceContext;
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

    private FlakeGenerator generator;

    @Override
    public void initialize(FlakeSequence annotation, Member member, GeneratorCreationContext context) {
        log.info("Initializing HibernateFlakeIdGenerator {} for {}#{}", this.hashCode(), member.getDeclaringClass(), member.getName());
        this.generator = makeGenerator(context.getPersistentClass().getRootTable());
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return EnumSet.of(EventType.INSERT);
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue, EventType eventType) {
        if (log.isDebugEnabled()) { // Avoid unnecessary array allocations
            log.debug("Generate: FlakeIdGenerator: {}, generating ID for: {} ({})",
                    System.identityHashCode(this), owner, System.identityHashCode(owner));
        }

        return generator.nextId();
    }

    private static synchronized FlakeGenerator makeGenerator(Table table) {
        return GeneratorsRegistry.getInstance().getGeneratorsMap().computeIfAbsent(table.toString(),
                c -> new SynchronizedFlakeGenerator(
                        SunflakeConfiguration.getWorkerId(),
                        SunflakeConfiguration.getGlobalRules()
                ));
    }
}
