package com.sb.flake;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableInitializerTest extends AbstractHibernateTest {

    @AfterEach
    public void dropTable() {
        session.doWork(connection -> {
            try (var stmt = connection.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS " + SunflakeConstants.KEEP_ALIVE_TABLE_NAME);
            }
        });
    }

    @Test
    public void GivenUnknownTableExistance_WhenCreateKeepAliveTableWithSession_ThenNTableExists() {
        System.out.println("Table exists already? " + TableInitializer.checkTableExists(session, SunflakeConstants.KEEP_ALIVE_TABLE_NAME));

        TableInitializer.createKeepAliveTable(super.session);

        assertTrue(TableInitializer.checkTableExists(session, SunflakeConstants.KEEP_ALIVE_TABLE_NAME));
    }

    @Test
    public void GivenUnknownTableExistance_WhenCreateKeepAliveTableWithEntityManager_ThenNTableExists() {
        System.out.println("Table exists already? " + TableInitializer.checkTableExists(session, SunflakeConstants.KEEP_ALIVE_TABLE_NAME));

        EntityManager em = super.getEntityManager();
        TableInitializer.createKeepAliveTable(em);

        assertTrue(TableInitializer.checkTableExists(session, SunflakeConstants.KEEP_ALIVE_TABLE_NAME));
    }
}