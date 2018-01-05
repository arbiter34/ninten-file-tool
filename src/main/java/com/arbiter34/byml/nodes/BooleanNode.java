package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;

import java.io.IOException;

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

    public void write(final BinaryAccessFile file) throws IOException {
        file.writeUnsignedInt(value ? 1L : 0L);
    }

    @Override
    public short getNodeType() {
        return NODE_TYPE;
    }
}
