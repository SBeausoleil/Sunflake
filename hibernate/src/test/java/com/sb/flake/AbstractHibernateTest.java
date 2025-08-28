package com.sb.flake;

import com.sb.flake.entities.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractHibernateTest {
    private static final Logger log = LoggerFactory.getLogger(AbstractHibernateTest.class);
    protected static SessionFactory sessionFactory;
    protected Session session;
    protected Transaction transaction;

    @BeforeAll
    public static void bootstrap() {
        try (StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build()) {
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClasses(
                            TestEntityA.class,
                            TestEntityB.class,
                            ParentEntityA.class,
                            ChildA.class,
                            ChildB.class
                    )
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e) {
            log.error("Exception whilst bootstrapping Hibernate: ", e);
        }
    }

    @BeforeEach
    public void openSessionAndTransaction() {
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();
        log.debug("Session and transaction opened");
    }

    @AfterEach
    public void rollbackTransactionAndCloseSession() {
        transaction.rollback();
        session.close();
        log.debug("Session and transaction closed");
    }

    public EntityManager getEntityManager() {
        EntityManagerFactory emf = session.getEntityManagerFactory();
        return emf.createEntityManager();
    }
}
