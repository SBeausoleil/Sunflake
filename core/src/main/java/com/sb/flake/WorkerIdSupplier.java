package com.sb.flake;

/**
 * A supplier for the unique ID of this process across all servers that connect to the same database.
 * The class must have a static method with the following signature:
 * {@code public static WorkerIdSupplier getInstance(SmartProperties properties)}
 * The method will be called during configuration to get the worker id of this process.
 */
public interface WorkerIdSupplier {
    /**
     * Get the unique ID of this process across all servers that connect to the same database.
     * <p>
     *     A worker ID may be reused, as long as there is no other active processes on this or other servers currently using it.
     * </p>
     * @param maxLength the maximum number of bits required to represent the worker ID
     * @return the unique ID of this process across all servers that connect to the same database
     */
    long getWorkerId(int maxLength);
}
