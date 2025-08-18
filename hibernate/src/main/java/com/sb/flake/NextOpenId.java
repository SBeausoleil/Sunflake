package com.sb.flake;

import systems.helius.commons.SmartProperties;

import java.util.OptionalLong;

public class NextOpenId implements AlternativeWorkerIdSupplier {

    @Override
    public long getWorkerId(int maxLength) {
        // This method should be implemented to return a worker ID.
        // For now, we throw an UnsupportedOperationException to indicate that this method is not implemented.
        throw new UnsupportedOperationException("Method not implemented yet.");
    }

    @Override
    public OptionalLong retryGetWorkerId(int maxLength, int nthRetry) {
        return OptionalLong.of(getWorkerId(maxLength));
    }

    public static WorkerIdSupplier getInstance(SmartProperties props) {
        return new NextOpenId();
    }
}
