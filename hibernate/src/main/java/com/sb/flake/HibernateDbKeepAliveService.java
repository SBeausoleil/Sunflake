package com.sb.flake;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.service.spi.Startable;

import java.time.LocalDateTime;

public class HibernateDbKeepAliveService implements KeepAliveService, Startable {

    private final EntityManagerFactory emf;

    public HibernateDbKeepAliveService() {
        this.emf = Persistence.createEntityManagerFactory("sunflake.hibernate");
    }

    @Override
    public AliveMarker aquireWorkerId(long workerId, WorkerIdSupplier alternativeSupplier, int maxTries, int maxLength) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            JpaAliveMarker existing = em.find(JpaAliveMarker.class, workerId);
            if (existing == null) {
                JpaAliveMarker marker = new JpaAliveMarker(workerId, maxLength, LocalDateTime.now());
                marker
            }

            em.persist(marker);
            em.getTransaction().commit();
            return marker;
        } catch (Exception e) {
            // Handle exceptions, possibly retry with alternativeSupplier
            e.printStackTrace();
        }

        return null; // TODO
    }

    @Override
    public LocalDateTime renewMarker(AliveMarker marker) {
        var now = LocalDateTime.now();
        // TODO
        return now;
    }

    @Override
    public void start() {

    }
}
