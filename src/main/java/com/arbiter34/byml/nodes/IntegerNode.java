package com.arbiter34.byml.nodes;

public class IntegerNode implements Node {
    public static final short NODE_TYPE = 0xD1;

    private final int value;

    public IntegerNode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static IntegerNode parse(long value) {
        return new IntegerNode((int)value);
    }
}
