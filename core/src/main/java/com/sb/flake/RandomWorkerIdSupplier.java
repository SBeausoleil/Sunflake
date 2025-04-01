package com.sb.flake;

import systems.helius.commons.SmartProperties;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomWorkerIdSupplier implements WorkerIdSupplier {
    @Override
    public long getWorkerId(int maxLength) {
        final long BIT_MASK = (1L << maxLength) - 1; // Bitmask required due to input potentially being negative
        final long MODULO = 1L << maxLength;

        try {
            return (SecureRandom.getInstanceStrong().nextLong() % MODULO) & BIT_MASK;
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("Your platform has no strong random number generation method!", e);
        }
    }

    public static WorkerIdSupplier getInstance(SmartProperties props) {
        return new RandomWorkerIdSupplier();
    }
}
