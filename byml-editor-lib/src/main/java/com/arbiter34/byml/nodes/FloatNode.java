package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

public class FloatNode implements Node<Float> {
    public static final short NODE_TYPE = 0xD2;

    private Float value;

    @JsonCreator
    public FloatNode(@JsonProperty("value") Float value) {
        this.value = value;
    }

    public Float getValue() {
        return value;
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }

    public static FloatNode parse(long value) {
        final byte[] buffer = new byte[4];
        buffer[0] = (byte)(value >>> 24);
        buffer[1] = (byte)(value >>> 16);
        buffer[2] = (byte)(value >>> 8);
        buffer[3] = (byte) value;
        return new FloatNode(ByteBuffer.wrap(buffer).getFloat());
    }

    public void write(final BinaryAccessFile file) throws IOException {
        int bits = Float.floatToIntBits(value);
        file.writeUnsignedInt(bits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FloatNode that = (FloatNode) o;
        return that.getValue() != null && Float.floatToIntBits(that.value) == Float.floatToIntBits(value);
    }

    @Override
    @JsonGetter("nodeType")
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
