package com.sb.flake;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.service.spi.Startable;
import systems.helius.commons.annotations.Internal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HibernateDbKeepAliveService implements KeepAliveService, Startable {

    private final EntityManagerFactory emf;

    public HibernateDbKeepAliveService() {
        this.emf = Persistence.createEntityManagerFactory("sunflake.hibernate");
    }

    @Override
    public JpaAliveMarker aquireWorkerId(AliveMarker desired, WorkerIdSupplier alternativeSupplier, int maxTries, int maxLength)
            throws WorkerIdReservationException {
        JpaAliveMarker jpaAliveMarker;
        if (desired instanceof JpaAliveMarker jpaMarker) {
            jpaAliveMarker = jpaMarker;
        } else {
            jpaAliveMarker = new JpaAliveMarker(desired);
        }
        return aquireWorkerId(jpaAliveMarker, alternativeSupplier, maxTries, maxLength, null);
    }

    @Internal
    protected JpaAliveMarker aquireWorkerId(JpaAliveMarker desired, WorkerIdSupplier alternativeSupplier,
                                            int maxTries, int maxLength,
                                            @Nullable List<Long> pastTries)
            throws WorkerIdReservationException {
        try (EntityManager em = emf.createEntityManager()) {
            int reserve = SunflakeConfiguration.getReserveDuration();
            LocalDateTime renewalTime = LocalDateTime.now();
            LocalDateTime reservedUntil = renewalTime.plusSeconds(reserve);

            em.getTransaction().begin();
            JpaAliveMarker existing = em.find(JpaAliveMarker.class, desired); // TODO: make sure this is SELECT FOR UPDATE
            JpaAliveMarker acquired = null;

            // Brand new, was never reserved before
            if (existing == null) {
                acquired = desired;
                acquired.setReservedUntil(reservedUntil);
                acquired.setLastRenewedTime(renewalTime);

                em.persist(acquired);

                // Marker in DB is expired
            } else if (existing.getReservedUntil()
                    .orElseThrow(() -> new IllegalStateException("Markers in the database should not have any null fields!"))
                    .isBefore(LocalDateTime.now())) {
                // Marker exists but is expired, renew it
                existing.setReservedUntil(reservedUntil);
                existing.setLastRenewedTime(renewalTime);
                acquired = em.merge(existing);

                // Marker exists and is still acquired
            } else {
                if (desired.equals(existing)) { // Means that this process owns the one in the DB
                    // Renew it early
                    existing.setReservedUntil(reservedUntil);
                    existing.setLastRenewedTime(renewalTime);
                    acquired = em.merge(existing);
                } else { // Someone else owns it
                    pastTries = Objects.requireNonNullElseGet(pastTries, () -> new ArrayList<>(maxTries));
                    pastTries.add(desired.getWorkerId());
                    if (pastTries.size() < maxTries) {
                        // Try to acquire a new one
                        long nextId = alternativeSupplier.getWorkerId(maxLength);
                        acquired = aquireWorkerId(new JpaAliveMarker(nextId), alternativeSupplier, maxTries, maxLength, pastTries);
                    } else {
                        // No more tries left, throw an exception
                        throw new WorkerIdReservationException(pastTries.stream().mapToLong(Long::longValue).toArray());
                    }
                }
            }

            em.getTransaction().commit();
            return acquired;
        } catch (WorkerIdReservationException e) {
            // Rethrow the exception to indicate failure in acquiring the worker ID
            throw e;
        } catch (Exception e) {
            pastTries = Objects.requireNonNullElseGet(pastTries, () -> new ArrayList<>(maxTries));
            pastTries.add(desired.getWorkerId());
            throw new WorkerIdReservationException(pastTries.stream().mapToLong(Long::longValue).toArray(), e);
        }
    }

    @Override
    public void start() {

    }
}
