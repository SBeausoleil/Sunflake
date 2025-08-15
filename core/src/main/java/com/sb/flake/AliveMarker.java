package com.sb.flake;

import systems.helius.commons.annotations.Unstable;

import java.time.LocalDateTime;

/**
 * Liveliness marker for a worker ID, indicating that the worker ID is in use by this process.
 */
public interface AliveMarker {
    /**
     * Get the worker ID that this marker represents.
     * @return the worker ID
     */
    long getWorkerId();
    /**
     * Get how long this marker is valid for, in seconds.
     * @implNote must be greater than zero, and should be at least 10 seconds to avoid excessive renewals
     * @return the expiration time in seconds
     */
    long getMaxLifeLength();
    /**
     * Get the last time this marker was renewed.
     * @return the last renewed time
     */
    LocalDateTime getLastRenewedTime();
    /**
     * Renew the marker to indicate that this worker ID is still in use.
     * @implNote this method must be called before the expiration time is reached, otherwise the marker will be considered expired
     * and the worker ID may be reused by another process.
     * @return true if the marker was successfully renewed, false otherwise (e.g. if the marker has become in use by another process)
     */
    @Unstable
    boolean renew();
}
