package com.arbiter34.file.util;

import java.nio.ByteBuffer;
import java.util.Optional;

public class FloatUtil {

    public static Float parse(final long value) {
        final byte[] buffer = new byte[4];
        buffer[0] = (byte)(value >>> 24);
        buffer[1] = (byte)(value >>> 16);
        buffer[2] = (byte)(value >>> 8);
        buffer[3] = (byte) value;
        return ByteBuffer.wrap(buffer).getFloat();
    }

    public static float parseNullSafe(final long value) {
        return Optional.ofNullable(parse(value)).orElse(0f);
    }
}
