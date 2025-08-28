package com.sb.flake;

import com.sb.flake.exceptions.WorkerIdReservationException;

/**
 * A service that manages the keep-alive markers for worker IDs.
 */
public interface KeepAliveService {
    /**
     * Acquire a worker ID by creating a marker in the database.
     * @param desired the desired worker ID marker to acquire/renew.
     * @param alternativeSupplier
     * @param maxTries
     * @param maxLength
     * @return the marker that was created. Does not have to be the same instance as the desired marker,
     * so one should not assume that the marker returned is the same as the one passed in.
     */
    AliveMarker aquireWorkerId(AliveMarker desired, WorkerIdSupplier alternativeSupplier, int maxTries, int maxLength) throws WorkerIdReservationException;
}
