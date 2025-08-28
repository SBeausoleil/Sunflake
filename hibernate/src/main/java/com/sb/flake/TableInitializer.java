package com.sb.flake;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.hibernate.Session;

class TableInitializer {
    /**
     * Creates the keep-alive table if it does not exist.
     */
    @Transactional
    static void createKeepAliveTable(EntityManager em) {
        em.getTransaction().begin();

        Query createTable = em.createNativeQuery("CREATE TABLE IF NOT EXISTS " + SunflakeConstants.KEEP_ALIVE_TABLE_NAME +
                " (workerId BIGINT PRIMARY KEY, reservedUntil TIMESTAMP, lastRenewedTime TIMESTAMP)");
        createTable.executeUpdate();

        em.getTransaction().commit();
    }

    static void createKeepAliveTable(Session session) {
        session.doWork(connection -> {
            try (var stmt = connection.createStatement()) {
                String sql = "CREATE TABLE IF NOT EXISTS " + SunflakeConstants.KEEP_ALIVE_TABLE_NAME +
                        " (workerId BIGINT PRIMARY KEY, reservedUntil TIMESTAMP, lastRenewedTime TIMESTAMP)";
                stmt.execute(sql);
            }
        });
    }

    /**
     * Checks if a table exists in the database.
     *
     * @param session   The Hibernate session to use for the check.
     * @param tableName The name of the table to check for existence.
     * @return true if the table exists, false otherwise.
     */
    static boolean checkTableExists(Session session, String tableName) {
        return session.doReturningWork(connection -> {
            var metaData = connection.getMetaData();
            var tables = metaData.getTables(null, null, tableName, null /*new String[]{"TABLE"}*/);
            return tables.next();
        });
    }
}
