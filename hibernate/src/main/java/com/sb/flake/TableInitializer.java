package com.sb.flake;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.service.spi.Startable;

public class TableInitializer implements Startable {

    @PersistenceContext
    private EntityManager em;

    @Override
    public void start() {
        EntityManager em = SunflakeJpaContext.getInstance().getEntityManager();
        em.createNativeQuery("CREATE TABLE IF NOT EXISTS " + SunflakeConstants.KEEP_ALIVE_TABLE_NAME +
                " (workerId BIGINT PRIMARY KEY, reservedUntil TIMESTAMP, lastRenewedTime TIMESTAMP)").executeUpdate();
    }
}
