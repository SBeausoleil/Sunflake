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
        for (int i = bits.length - 1; i >= 0; i--) {
            sb.append(bits[i]);
            if (i == rules.getSequenceSize())
                sb.append('_');
            else if (i == rules.getSequenceSize() + rules.getWorkerSize())
                sb.append('_');
            else if (i == rules.getSequenceSize() + rules.getWorkerSize() + rules.getTimestampSize())
                sb.append('_');
        }
        return sb.toString();
    }
}
