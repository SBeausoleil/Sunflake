package com.sb.flake;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class AbstractHibernateTest {
    protected static SessionFactory sessionFactory;
    protected Session session;
    protected Transaction transaction;

    @BeforeAll
    public static void bootstrap() {
        try (StandardServiceRegistry registry = new StandardServiceRegistryBuilder().build()) {
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(TestEntity.class)
                    .buildMetadata()
                    .buildSessionFactory();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    @BeforeEach
    public void openSessionAndTransaction() {
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();
        System.out.println("Session and transaction opened");
    }

    @AfterEach
    public void rollbackTransactionAndCloseSession() {
        transaction.rollback();
        session.close();
        System.out.println("Session and transaction closed");
    }


}
