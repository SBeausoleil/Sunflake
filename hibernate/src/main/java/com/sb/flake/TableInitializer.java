package com.sb.flake;

import jakarta.persistence.EntityManager;
import org.hibernate.service.spi.Startable;
import org.hibernate.tool.hbm2ddl.SchemaExport;

public class TableInitializer implements Startable {
    @Override
    public void start() {
        EntityManager em = SunflakeJpaContext.getInstance().getEntityManager();
        em.createNativeQuery("CREATE TABLE IF NOT EXISTS " + SunflakeConstants.KEEP_ALIVE_TABLE_NAME +
                " (workerId BIGINT PRIMARY KEY, reservedUntil TIMESTAMP, lastRenewedTime TIMESTAMP)").executeUpdate();
    }
}
