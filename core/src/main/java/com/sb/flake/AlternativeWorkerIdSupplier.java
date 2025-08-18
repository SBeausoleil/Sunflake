package com.sb.flake;

import systems.helius.commons.annotations.Unstable;

import java.util.OptionalLong;

/**
 * A supplier for the unique ID of this process across all servers that connect to the same database,
 * that can also attempt to provide an alternative worker ID if the first attempt fails.
 * <p>
 * The class must have a static method with the following signature:
 * {@code public static AlternativeWorkerIdSupplier getInstance(SmartProperties properties)}.
 * The method will be called during configuration if there is a need to attempt a backup method.
 * </p>
 * <p>
 *     <b>Unstable:</b> May be rolled into WorkierIdSupplier in the future...
 * </p>
 */
@Unstable
public interface AlternativeWorkerIdSupplier extends WorkerIdSupplier {
    /**
     * Attempt to get a different worker ID if the first, ideal, attempt fails.
     *
     * @param maxLength the maximum number of bits required to represent the worker ID. Will be a number between 1 and 60.
     * @param nthRetry  the number of the retry attempt, starting from 1.
     * @return the unique ID of this process across all servers that connect to the same database.
     * Empty if no worker ID could be obtained.
     */
    OptionalLong retryGetWorkerId(int maxLength, int nthRetry);
}
