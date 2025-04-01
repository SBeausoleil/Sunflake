package com.sb.flake;

import org.junit.jupiter.api.Test;

import java.net.SocketException;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class PhysicalAddressWorkerIdSupplierTest {

    @Test
    void GivenNewInstance_WhenGetIdWith48bits_ThenProvideFullMAC() throws SocketException, UnknownHostException {
        var supplier = PhysicalAddressWorkerIdSupplier.getInstance(null);
        long mac = PhysicalAddressWorkerIdSupplier.getPhysicalAddress();
        assertEquals(mac, supplier.getWorkerId(48));
    }
}