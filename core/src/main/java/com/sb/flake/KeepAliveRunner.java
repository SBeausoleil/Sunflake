package com.sb.flake;

import com.sb.flake.exceptions.WorkerIdRenewalException;

/**
 * Runnable that is executed periodically to renew the worker ID.
 */
public class KeepAliveRunner implements Runnable {
    private final KeepAliveService keepAliveService;

    public KeepAliveRunner(KeepAliveService keepAliveService) {
        this.keepAliveService = keepAliveService;
    }

    @Override
    public void run() {
        SunflakeConfiguration.getAliveMarker().ifPresent(present -> {
            try {
                AliveMarker marker = keepAliveService.aquireWorkerId(present,
                        SunflakeConfiguration.getFallbackWorkerIdSupplier().orElse(null),
                        SunflakeConfiguration.getMaxTries(),
                        SunflakeConfiguration.getGlobalRules().getWorkerSize());
                SunflakeConfiguration.setAliveMarker(marker);
            } catch (Exception e) {
                throw new WorkerIdRenewalException(e);
            }
        });
    }
}
