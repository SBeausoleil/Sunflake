package com.sb.flake;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RandomWorkerIdSupplier implements WorkerIdSupplier {
    @Override
    public long getWorkerId(int maxLength) {
        if (maxLength < 1 || maxLength > 60) {
            throw new IllegalArgumentException("Worker ID length must be between 1 and 60 bits!");
        }

        final long BIT_MASK = (1L << maxLength) - 1;
        final long MODULO = 1L << maxLength;

        try {
            return (SecureRandom.getInstanceStrong().nextLong() % MODULO) & BIT_MASK;
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException("Your platform has no strong random number generation method!", e);
        }
    }
}
