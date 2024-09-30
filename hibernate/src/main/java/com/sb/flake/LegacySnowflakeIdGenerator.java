package com.sb.flake;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.BulkInsertionCapableIdentifierGenerator;
import org.hibernate.id.IdentifierGenerator;

import java.time.Instant;

public class LegacySnowflakeIdGenerator implements IdentifierGenerator, BulkInsertionCapableIdentifierGenerator {
    private FlakeGenerator flakeGenerator;

    @PersistenceContext
    private EntityManager entityManager;

    private SynchronizedFlakeGenerator generator = new SynchronizedFlakeGenerator(Instant.parse("2024-01-01 00:00:00"), 1, GenerationRules.SNOWFLAKE);

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) {
        System.out.println("ID Provider: " + System.identityHashCode(this));
        System.out.println("Generator: " + System.identityHashCode(generator));
        return generator.nextId();
    }
}
