package com.sb.flake;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class RandomWorkerIdSupplierTest {
    @Test
    void getWorkerId_max10() {
        var supplier = new RandomWorkerIdSupplier();
        long workerId = supplier.getWorkerId(10);
        assertTrue(workerId >= 0 && workerId < 1024, workerId + " is not between 0 and 1024.");
    }

    @Test
    void getWorkerId_max4() {
        var supplier = new RandomWorkerIdSupplier();
        long workerId = supplier.getWorkerId(4);
        assertTrue(workerId >= 0 && workerId < 16, " is not between 0 and 16");
    }

    @Test
    void getWorkerId_noDuplicate() {
        var supplier = new RandomWorkerIdSupplier();
        var known = new HashSet<Long>();
        for (int i = 0; i < 1000; i++) {
            long workerId = supplier.getWorkerId(32);
            assertTrue(known.add(workerId));
        }
    }
}