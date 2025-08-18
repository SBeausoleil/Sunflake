package com.sb.flake;

import systems.helius.commons.annotations.Unstable;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Liveliness marker for a worker ID, indicating that the worker ID is in use by this process.
 */
public interface AliveMarker {
    /**
     * Get the worker ID that this marker represents.
     * @return the worker ID
     */
    long getWorkerId();
    void setWorkerId(long workerId);

    /**
     * Get the moment until which this marker is reserved and should not be reused by another process.
     * @return the reserved until time. Empty if the marker is not reserved yet.
     */
    Optional<LocalDateTime> getReservedUntil();
    void setReservedUntil(LocalDateTime reservedUntil);

    /**
     * Get the last time this marker was renewed.
     * @return the last renewed time. Empty if the marker has never been renewed.
     */
    Optional<LocalDateTime> getLastRenewedTime();
    void setLastRenewedTime(LocalDateTime lastRenewedTime);

}
