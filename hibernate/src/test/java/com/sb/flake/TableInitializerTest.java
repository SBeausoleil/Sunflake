package com.sb.flake;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableInitializerTest extends AbstractHibernateTest {

    @Test
    @Transactional
    public void GivenUnknownTableExistance_WhenCreateKeepAliveTable_ThenNTableExists() {
        System.out.println("Table exists already? " + TableInitializer.checkTableExists(session, SunflakeConstants.KEEP_ALIVE_TABLE_NAME));

        EntityManager em = super.getEntityManager();
        TableInitializer.createKeepAliveTable(em);

        assertTrue(TableInitializer.checkTableExists(session, SunflakeConstants.KEEP_ALIVE_TABLE_NAME));
    }
}