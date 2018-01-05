package com.arbiter34.byml.nodes;

public class StringNode implements Node {
    public static final short NODE_TYPE = 0xA0;

    private final long index;
    private final String value;

    public StringNode(long index, String value) {
        this.index = index;
        this.value = value;
    }

    public long getIndex() {
        return index;
    }

    public String getValue() {
        return value;
    }

    public static StringNode parse(final StringTableNode stringNameTable, final long value) {
        return new StringNode(value, stringNameTable.getEntries().get((int)value));
    }
}
