package com.sb.flake;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SunflakeJpaContext {
    private static final Logger log = LoggerFactory.getLogger(SunflakeJpaContext.class);
    private static SunflakeJpaContext instance;

    private final EntityManagerFactory emf;
    private final KeepAliveService keepAliveService;

     private SunflakeJpaContext(EntityManagerFactory emf) {
        this.emf = emf;
        this.keepAliveService = new HibernateDbKeepAliveService();
    }

    public static synchronized SunflakeJpaContext initialize(EntityManagerFactory emf) throws WorkerIdReservationException {
        if (instance == null) {
            instance = new SunflakeJpaContext(emf);
            TableInitializer.createKeepAliveTable(instance.getEntityManager());

            AliveMarker keepAliveToken = new JpaAliveMarker(SunflakeConfiguration.getWorkerId());
            keepAliveToken = instance.keepAliveService.aquireWorkerId(keepAliveToken, new NextOpenId(),
                    SunflakeConfiguration.getMaxTries(), SunflakeConfiguration.getGlobalRules().WORKER_ID_SIZE);
            SunflakeConfiguration.setAliveMarker(keepAliveToken);

            // TODO start a background thread to renew the keep-alive token periodically
        } else {
            log.warn("SunflakeJpaContext is already initialized.");
        }
        return instance;
    }

    public static SunflakeJpaContext getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SunflakeJpaContext is not initialized. Call initialize(EntityManagerFactory) first.");
        }
        return instance;
    }

    /**
     * Get a new EntityManager instance. The caller is responsible for closing it.
     *
     * @return a new EntityManager instance
     */
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
