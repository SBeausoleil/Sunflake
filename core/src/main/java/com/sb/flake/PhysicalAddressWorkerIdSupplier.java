package com.sb.flake;

import com.sb.flake.exceptions.InitializationException;
import systems.helius.commons.SmartProperties;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

public class PhysicalAddressWorkerIdSupplier implements WorkerIdSupplier {
    private final long PHYSICAL_ADDRESS;

    public PhysicalAddressWorkerIdSupplier() throws SocketException, UnknownHostException {
        PHYSICAL_ADDRESS = getPhysicalAddress();
    }

    static long getPhysicalAddress() throws UnknownHostException, SocketException {
        // Code from Baeldung: https://www.baeldung.com/java-mac-address
        InetAddress localHost = InetAddress.getLocalHost();
        NetworkInterface ni = NetworkInterface.getByInetAddress(localHost);
        byte[] hardwareAddress = ni.getHardwareAddress();

        long address = 0;
        for (int i = 0; i < hardwareAddress.length; i++) {
            address |= ((long) hardwareAddress[i]) << (i * 8);
        }
        return address;
    }

    public static WorkerIdSupplier getInstance(SmartProperties properties) {
        try {
            return new PhysicalAddressWorkerIdSupplier();
        } catch (SocketException | UnknownHostException e) {
            throw new InitializationException(e);
        }
    }

    @Override
    public long getWorkerId(int maxLength) {
        return PHYSICAL_ADDRESS % (1L << maxLength);
    }
}
