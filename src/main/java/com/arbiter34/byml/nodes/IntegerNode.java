package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;

import java.io.IOException;

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

    public void write(final BinaryAccessFile file) throws IOException {
        file.writeUnsignedInt(value);
    }

    @Override
    public short getNodeType() {
        return NODE_TYPE;
    }
}
