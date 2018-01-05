package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.Optional;

public class HashNode implements Node<Long> {
    public static final short NODE_TYPE = 0xD3;

    private long value;

    @JsonCreator
    public HashNode(@JsonProperty("value") long value) {
        this.value = value;
    }

    public static HashNode parse(long value) {
        return new HashNode(value);
    }

    public void write(final BinaryAccessFile file) throws IOException {
        file.writeUnsignedInt(value);
    }

    public boolean equals(final long other) {
        return value == other;
    }

    @Override
    @JsonGetter("nodeType")
    public short getNodeType() {
        return NODE_TYPE;
    }

    @Override
    public boolean eq(Long aLong) {
        return aLong != null && aLong.equals(value);
    }

    @Override
    public void setValue(Long aLong) {
        this.value = Optional.ofNullable(aLong).orElse(value);
    }

    @Override
    public Long getValue() {
        return value;
    }
}
