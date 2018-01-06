package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.Objects;

public class StringNode implements Node<String> {
    public static final short NODE_TYPE = 0xA0;

    private String value;

    public StringNode(String value) {
        this.value = value;
    }

    public static StringNode parse(final StringTableNode stringNameTable, final long value) {
        return new StringNode(stringNameTable.getEntries().get((int)value));
    }

    public void write(final BinaryAccessFile file, final StringTableNode stringTable) throws IOException {
        file.writeUnsignedInt(stringTable.getEntries().indexOf(value));
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void setValue(String s) {
        this.value = s;
    }

    @Override
    public boolean eq(final String other) {
        return value.equals(other);
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
        StringNode that = (StringNode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

        return Objects.hash(value);
    }
}
