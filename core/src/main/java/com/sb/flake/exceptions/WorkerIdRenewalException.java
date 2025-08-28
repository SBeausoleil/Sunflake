package com.sb.flake.exceptions;

/**
 * Exception thrown if the automated worker ID renewal thread fails to acquire
 * any ID during renewal.
 */
public class WorkerIdRenewalException extends RuntimeException {
    public WorkerIdRenewalException(String message) {
        super(message);
    }

    public WorkerIdRenewalException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkerIdRenewalException(Throwable cause) {
        super(cause);
    }
}
