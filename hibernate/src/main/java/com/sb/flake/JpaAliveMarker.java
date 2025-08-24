package com.sb.flake;

import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a marker in the DB that indicates that this worker id is in use.
 */
@Entity
@Table(name = SunflakeConstants.KEEP_ALIVE_TABLE_NAME)
public class JpaAliveMarker implements AliveMarker {
    @Id
    private long workerId;
    @Nullable
    private LocalDateTime reservedUntil;
    @Nullable
    private LocalDateTime lastRenewedTime;

    /**
     * Default constructor for JPA.
     * This constructor is required by JPA to create instances of this entity.
     */
    @SuppressWarnings("unused")
    protected JpaAliveMarker() {
        // Default constructor for JPA
    }

    public JpaAliveMarker(AliveMarker prototype) {
        this.workerId = prototype.getWorkerId();
        this.reservedUntil = prototype.getReservedUntil().orElse(null);
        this.lastRenewedTime = prototype.getLastRenewedTime().orElse(null);
    }

    public JpaAliveMarker(long workerId) {
        this(workerId, null, null);
    }

    public JpaAliveMarker(long workerId, @Nullable LocalDateTime reservedUntil, @Nullable LocalDateTime lastRenewedTime) {
        this.workerId = workerId;
        this.reservedUntil = reservedUntil;
        this.lastRenewedTime = lastRenewedTime;
    }

    @Override
    public long getWorkerId() {
        return workerId;
    }

    @Override
    public Optional<LocalDateTime> getReservedUntil() {
        return Optional.ofNullable(reservedUntil);
    }

    @Override
    public Optional<LocalDateTime> getLastRenewedTime() {
        return Optional.ofNullable(lastRenewedTime);
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

    public void setReservedUntil(@Nullable LocalDateTime reservedUntil) {
        this.reservedUntil = reservedUntil;
    }

    public void setLastRenewedTime(@Nullable LocalDateTime lastRenewedTime) {
        this.lastRenewedTime = lastRenewedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof JpaAliveMarker that)) return false;
        return workerId == that.workerId && Objects.equals(reservedUntil, that.reservedUntil) && Objects.equals(lastRenewedTime, that.lastRenewedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(workerId, reservedUntil, lastRenewedTime);
    }
}
