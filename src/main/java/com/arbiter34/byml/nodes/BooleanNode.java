package com.arbiter34.byml.nodes;

public class BooleanNode implements Node {
    public static final short NODE_TYPE = 0xD0;

    private final boolean value;

    public BooleanNode(boolean value) {
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }

    public static BooleanNode parse(long value) {
        return new BooleanNode(value == 1);
    }
}
