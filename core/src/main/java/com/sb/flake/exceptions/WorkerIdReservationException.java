package com.sb.flake.exceptions;

import java.util.Arrays;

/**
 * An exception that is thrown when the acquisition or renewal of a worker ID marker fails,
 * has become in use by another process and the attempted alternatives also failed,
 * or when an exception occurred while trying to reserve a worker ID.
 */
public class WorkerIdReservationException extends Exception {
    private final long[] triedIds;

    /**
     * Constructs a new RenewalException with the specified worker ID.
     *
     * @param triedIds all the IDs that were tried to reserve, in chronological order.
     */
    public WorkerIdReservationException(long[] triedIds) {
        super("Failed to reserve any of the following worker ID: "+ Arrays.toString(triedIds));
        this.triedIds = triedIds;
    }

    public WorkerIdReservationException(long[] triedIds, Throwable cause) {
        super("Failed to reserve any of the following worker ID: "+ Arrays.toString(triedIds), cause);
        this.triedIds = triedIds;
    }

    public long[] getTriedIds() {
        return triedIds;
    }
}
