package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.arbiter34.byml.util.NodeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArrayNode implements Node {
    public static final short NODE_TYPE = 0xC0;

    private final int numEntries;
    private final int[] nodeTypes;
    private final List<? extends Node> nodes;

    public ArrayNode(final int numEntries, final int[] nodeTypes, final List<? extends Node> nodes) {
        this.numEntries = numEntries;
        this.nodeTypes = nodeTypes;
        this.nodes = nodes;
    }

    public static ArrayNode parse(final StringTableNode nodeNameTable, final StringTableNode stringValueTable,
                                  final BinaryAccessFile file) throws IOException {
        long typeAndNumEntries = file.readUnsignedInt();
        final short nodeType = (short)(0x0000000000FF & (typeAndNumEntries >>> 24));
        if (nodeType != NODE_TYPE) {
            throw new IOException(String.format("Found invalid node type. Expected: %s Found: %s", NODE_TYPE, nodeType));
        }

        final int numEntries = (int)(0x00FFFFFF & typeAndNumEntries);
        final int[] nodeTypes = new int[numEntries];
        for (int i = 0; i < nodeTypes.length; i++) {
            nodeTypes[i] = file.readUnsignedByte();
        }
        long position = file.getFilePointer();
        if ((position % 4) != 0) {
            file.skipBytes((int)(4 - (position % 4)));
        }
        final List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < numEntries; i++) {
            final long value = file.readUnsignedInt();
            nodes.add(NodeUtil.parseNode(nodeNameTable, stringValueTable, file, (short)nodeTypes[i], value));
        }
        return new ArrayNode(numEntries, nodeTypes, nodes);
    }

    public int getNumEntries() {
        return numEntries;
    }

    public int[] getNodeTypes() {
        return nodeTypes;
    }

    public List<? extends Node> getNodes() {
        return nodes;
    }
}
