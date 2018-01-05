package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.arbiter34.byml.util.NodeUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArrayNode implements Node {
    public static final short NODE_TYPE = 0xC0;
    private static final int BYTE_ALIGNMENT = 4;

    private final int numEntries;
    private final int[] nodeTypes;
    private final List<? extends Node> nodes;

    private ArrayNode(final int numEntries, final int[] nodeTypes, final List<? extends Node> nodes) {
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
        NodeUtil.byteAlign(file, false);
        final List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < numEntries; i++) {
            final long value = file.readUnsignedInt();
            nodes.add(NodeUtil.parseNode(nodeNameTable, stringValueTable, file, (short)nodeTypes[i], value));
        }
        return new ArrayNode(numEntries, nodeTypes, nodes);
    }

    public void write(final StringTableNode nodeNameTable, final StringTableNode stringValueTable,
                      final BinaryAccessFile file) throws IOException {
        byte[] bytes = new byte[4];
        bytes[0] = (byte)NODE_TYPE;
        bytes[1] = (byte)(numEntries >>> 16);
        bytes[2] = (byte)(numEntries >>> 8);
        bytes[3] = (byte)(numEntries);
        file.write(bytes);
        for (int i = 0; i < nodeTypes.length; i++) {
            file.writeByte(nodeTypes[i]);
        }
        NodeUtil.byteAlign(file, true);
        long arrayEnd = file.getFilePointer() + (4 * numEntries);
        for (final Node node : nodes) {
            if (node instanceof ArrayNode || node instanceof  DictionaryNode) {
                file.writeUnsignedInt(arrayEnd);
                long lastPosition = file.getFilePointer();
                file.seek(arrayEnd);
                NodeUtil.writeNode(nodeNameTable, stringValueTable, file, node);
                arrayEnd = file.getFilePointer();
                file.seek(lastPosition);
            } else {
                NodeUtil.writeNode(nodeNameTable, stringValueTable, file, node);
            }
        }
        NodeUtil.byteAlign(file, true);
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

    @Override
    public short getNodeType() {
        return NODE_TYPE;
    }
}
