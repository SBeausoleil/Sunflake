package com.sb.flake;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class SunflakeJpaContext {
    private static final class InstanceHolder {
        private static final SunflakeJpaContext instance = new SunflakeJpaContext();
    }

    public static final String PERSISTENCE_UNIT_NAME = "sunflake.hibernate";
    private final EntityManagerFactory emf;

    public SunflakeJpaContext() {
        this.emf = Persistence.createEntityManagerFactory(SunflakeJpaContext.PERSISTENCE_UNIT_NAME);
    }

    public static SunflakeJpaContext getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Get a new EntityManager instance. The caller is responsible for closing it.
     * @return a new EntityManager instance
     */
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
