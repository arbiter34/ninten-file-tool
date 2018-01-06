package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class IntegerNode implements Node<Integer> {
    public static final short NODE_TYPE = 0xD1;

    private int value;

    @JsonCreator
    public IntegerNode(@JsonProperty("value") int value) {
        this.value = value;
    }

    public static IntegerNode parse(long value) {
        return new IntegerNode((int)value);
    }

    public void write(final BinaryAccessFile file) throws IOException {
        file.writeUnsignedInt(value);
    }

    @Override
    public boolean eq(final Integer other) {
        return other != null && other.equals(value);
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public void setValue(Integer integer) throws UnsupportedOperationException {
        this.value = Optional.ofNullable(integer).orElse(value);
    }

    @Override
    @JsonGetter("nodeType")
    public short getNodeType() {
        return NODE_TYPE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntegerNode that = (IntegerNode) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }
}
