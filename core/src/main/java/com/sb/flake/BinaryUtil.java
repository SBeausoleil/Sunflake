package com.sb.flake;

public final class BinaryUtil {
    private BinaryUtil() {
    }

    public static String toUnformattedBinary(long value) {
        return "0".repeat(Long.numberOfLeadingZeros(value))
                + Long.toBinaryString(value);
    }

    public static String toFormattedBinary(long value, GenerationRules rules) {
        String unformatted = toUnformattedBinary(value);
        StringBuilder sb = new StringBuilder(unformatted.length() + 2 + (rules.canUseSignBit() ? 1 : 0));
        char[] bits = unformatted.toCharArray();
        for (int i = 0; i < bits.length; i++) {
            if ((!rules.canUseSignBit() && i == Long.SIZE - rules.getTimestampSize() - rules.getWorkerSize() - rules.getSequenceSize()) // Sign bit
                    || i == Long.SIZE - rules.getWorkerSize() - rules.getSequenceSize() // End of timestamp
                    || i == Long.SIZE - rules.getSequenceSize()) // End of worker id
                sb.append(bits[i]);
        }
        return sb.toString();
    }
}
