package com.sb.flake;

public final class BinaryUtil {
    private BinaryUtil() {}

    public static String toUnformattedBinary(long value) {
        StringBuilder sb = new StringBuilder(Long.SIZE);
        for(int i = 0; i < Long.numberOfLeadingZeros(value); i++) {
            sb.append('0');
        }
        sb.append(Long.toBinaryString(value));
        return sb.toString();
    }

    public static String toFormattedBinary(long value, GenerationRules rules) {
        String unformatted = toUnformattedBinary(value);
        StringBuilder sb = new StringBuilder(unformatted.length() + 2 + (rules.canUseSignBit() ? 1 : 0));
        char[] bits = unformatted.toCharArray();
        for (int i = 0; i < bits.length; i++) {
            if (!rules.canUseSignBit() && i == Long.SIZE - rules.getTimestampSize() - rules.getWorkerSize() - rules.getSequenceSize())
                sb.append("_"); // Sign
            else if (i == Long.SIZE - rules.getWorkerSize() - rules.getSequenceSize())
                sb.append("_"); // End of timestamp
            else if (i == Long.SIZE - rules.getSequenceSize())
                sb.append("_"); // End of worker id
            sb.append(bits[i]);
        }
        return sb.toString();
    }
/*

    public static String toFormattedBinary(long snowflake) {
        String unformatted = toUnformattedBinary(snowflake);
        StringBuilder sb = new StringBuilder(unformatted.length() + 3); // Add _ for sign separation, _ for ts, _ for machine
        char[] bits = unformatted.toCharArray();
        for (int i = 0; i < unformatted.length(); i++) {
            sb.append(bits[i]);
            if (i == 0) // Sign bit
                sb.append('_');
            else if (i + 1 == 1 + TS_LENGTH) { // TS bits
                sb.append('_');
            } else if (i + 1 == 1 + TS_LENGTH + MACHINE_ID_LENGTH) { // Machine ID bits
                sb.append('_');
            }
        }
        return sb.toString();
    }*/
}
