package com.sb.flake;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceProviderResolver;
import jakarta.persistence.spi.PersistenceProviderResolverHolder;

public class SunflakeJpaContext {
    private static final class InstanceHolder {
        private static final SunflakeJpaContext instance = new SunflakeJpaContext();
    }

    public static final String PERSISTENCE_UNIT_NAME = "sunflake.hibernate";
    private final EntityManagerFactory emf;
    private boolean initialized = false;

    public SunflakeJpaContext() {
        PersistenceProviderResolver resolver = PersistenceProviderResolverHolder.getPersistenceProviderResolver();
        this.emf = resolver.getPersistenceProviders().get(0).createEntityManagerFactory(PERSISTENCE_UNIT_NAME, null);


        //  this.emf = Persistence.createEntityManagerFactory(SunflakeJpaContext.PERSISTENCE_UNIT_NAME);
    }

    public static SunflakeJpaContext getInstance() {
        return InstanceHolder.instance;
    }

    /**
     * Get a new EntityManager instance. The caller is responsible for closing it.
     *
     * @return a new EntityManager instance
     */
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
        //return null;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public synchronized void initialize() {
        if (!initialized) {
            TableInitializer ti = new TableInitializer();
            ti.start();
            initialized = true;
            System.out.println("Sunflake JPA context initialized.");
        }
    }
}
