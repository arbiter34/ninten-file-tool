package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;

public class PathTableNode implements Node {
    public static final short NODE_TYPE = 0xC3;

    public static PathTableNode parse(long offset, BinaryAccessFile file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public short getNodeType() {
        return NODE_TYPE;
    }
}
