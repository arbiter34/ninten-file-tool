package com.arbiter34.byml.nodes;

public class HashNode implements Node {
    public static final short NODE_TYPE = 0xD3;

    private final long value;

    public HashNode(long value) {
        this.value = value;
    }

    public static HashNode parse(long value) {
        return new HashNode(value);
    }
}
