package com.sb.flake;

import java.time.LocalDateTime;

/**
 * A service that manages the keep-alive markers for worker IDs.
 */
public interface KeepAliveService {
    /**
     * Acquire a worker ID by creating a marker in the database.
     * @param workerId
     * @param alternativeSupplier
     * @param maxTries
     * @param maxLength
     * @return the marker that was created
     */
    AliveMarker aquireWorkerId(long workerId, WorkerIdSupplier alternativeSupplier, int maxTries, int maxLength);

    /**
     * Renew the marker in the database to indicate that this worker ID is still in use.
     * @param marker the marker to renew
     */
    LocalDateTime renewMarker(AliveMarker marker) throws RenewalException;
}
