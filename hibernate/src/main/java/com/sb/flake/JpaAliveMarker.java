package com.sb.flake;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Represents a marker in the DB that indicates that this worker id is in use.
 */
@Entity
@Table(name = "sunflake_alive_marker")
public class JpaAliveMarker implements AliveMarker {
    /**
     * The worker id that is in use.
     */
    @Id
    protected long workerId;
    /**
     * The moment when this marker was created/renewed.
     */
    protected LocalDateTime moment;
    /**
     * The number of seconds after which this marker is considered expired.
     */
    protected int nSeconds;

    protected LocalDateTime lastRenewedTime;

    protected transient KeepAliveService keepAliveService;

    protected JpaAliveMarker() {
    }

    public JpaAliveMarker(long workerId, int nSeconds, LocalDateTime moment) {
        this.workerId = workerId;
        this.nSeconds = nSeconds;
        this.moment = moment;
    }

    public long getWorkerId() {
        return workerId;
    }

    public LocalDateTime getMoment() {
        return moment;
    }

    public void setMoment(LocalDateTime moment) {
        this.moment = moment;
    }

    public int getnSeconds() {
        return nSeconds;
    }

    @Override
    public boolean renew() {
        try {
            this.lastRenewedTime = keepAliveService.renewMarker(this);
            return true;
        } catch (RenewalException e) {
            return false;
        }
    }

    @Override
    public LocalDateTime getLastRenewedTime() {
        return null;
    }

    @Override
    public long getMaxLifeLength() {
        return 0;
    }
}
