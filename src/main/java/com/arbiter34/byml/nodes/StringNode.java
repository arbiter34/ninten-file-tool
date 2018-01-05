package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;

import java.io.IOException;

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

    public void write(final BinaryAccessFile file, final StringTableNode stringTable) throws IOException {
        file.writeUnsignedInt(stringTable.getEntries().indexOf(value));
    }

    @Override
    public short getNodeType() {
        return NODE_TYPE;
    }
}
