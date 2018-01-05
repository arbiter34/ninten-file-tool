package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;

import java.io.IOException;

public class HashNode implements Node {
    public static final short NODE_TYPE = 0xD3;

    private final long value;

    public HashNode(long value) {
        this.value = value;
    }

    public static HashNode parse(long value) {
        return new HashNode(value);
    }

    public void write(final BinaryAccessFile file) throws IOException {
        file.writeUnsignedInt(value);
    }

    @Override
    public short getNodeType() {
        return NODE_TYPE;
    }
}
