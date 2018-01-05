package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

public class FloatNode implements Node<Float> {
    public static final short NODE_TYPE = 0xD2;

    private Float value;

    public FloatNode(Float value) {
        this.value = value;
    }

    public Float getValue() {
        return value;
    }

    public static FloatNode parse(long value) {
        final byte[] buffer = new byte[4];
        buffer[0] = (byte)((value & 0xF000) >>> 24);
        buffer[0] = (byte)((value & 0x0F00) >>> 16);
        buffer[0] = (byte)((value & 0x00F0) >>> 8);
        buffer[0] = (byte)((value & 0x000F));
        return new FloatNode(ByteBuffer.wrap(buffer).getFloat());
    }

    public void write(final BinaryAccessFile file) throws IOException {
        int bits = Float.floatToIntBits(value);
        file.writeUnsignedInt(bits);
    }

    public boolean equals(final Float other) {
        return value.equals(other);
    }

    @Override
    public short getNodeType() {
        return NODE_TYPE;
    }

    @Override
    public boolean eq(Float aFloat) {
        return aFloat != null && aFloat.equals(value);
    }

    @Override
    public void setValue(Float aFloat) {
        this.value = Optional.ofNullable(aFloat).orElse(null);
    }
}
