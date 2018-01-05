package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;

public class PathTableNode {
    public static final short NODE_TYPE = 0xC3;

    public static PathTableNode parse(long offset, BinaryAccessFile file) {
        throw new UnsupportedOperationException();
    }

}
