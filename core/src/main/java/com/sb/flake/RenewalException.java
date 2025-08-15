package com.sb.flake;

/**
 * An exception that is thrown when a renewal of a worker ID marker fails.
 * This can happen if the marker is not found or has become in use by another process.
 */
public class RenewalException extends Exception {
    private final long failedWorkerId;

    /**
     * Constructs a new RenewalException with the specified worker ID.
     *
     * @param failedWorkerId the worker ID that failed to renew
     */
    public RenewalException(long failedWorkerId) {
        super("Failed to renew worker ID: " + failedWorkerId);
        this.failedWorkerId = failedWorkerId;
    }
}
