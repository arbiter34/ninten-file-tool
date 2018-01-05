package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.arbiter34.byml.util.NodeUtil;
import com.fasterxml.jackson.annotation.JsonGetter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArrayNode extends ArrayList<Node> implements Node<List<Node>> {
    public static final short NODE_TYPE = 0xC0;

    public ArrayNode() {
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
        final ArrayNode instance = new ArrayNode();
        for (int i = 0; i < numEntries; i++) {
            final long value = file.readUnsignedInt();
            instance.add(NodeUtil.parseNode(nodeNameTable, stringValueTable, file, (short)nodeTypes[i], value));
        }
        return instance;
    }

    public void write(final StringTableNode nodeNameTable, final StringTableNode stringValueTable,
                      final BinaryAccessFile file) throws IOException {
        final int numEntries = this.size();
        byte[] bytes = new byte[4];
        bytes[0] = (byte)NODE_TYPE;
        bytes[1] = (byte)(numEntries >>> 16);
        bytes[2] = (byte)(numEntries >>> 8);
        bytes[3] = (byte)(numEntries);
        file.write(bytes);
        for (int i = 0; i < numEntries; i++) {
            final short nodeType = NodeType.valueOfClazz(this.get(i).getClass()).getNodeType();
            file.writeByte(nodeType);
        }
        NodeUtil.byteAlign(file, true);
        long arrayEnd = file.getFilePointer() + (4 * numEntries);
        for (final Node node : this) {
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
        file.seek(arrayEnd);
    }

    @Override
    @JsonGetter("nodeType")
    public short getNodeType() {
        return NODE_TYPE;
    }

    @Override
    public boolean eq(List<Node> nodes) {
        return this.equals(nodes);
    }

    @Override
    public void setValue(List<Node> nodes) {
        this.clear();
        Optional.ofNullable(nodes).ifPresent(this::addAll);
    }

    @Override
    public List<Node> getValue() {
        return this;
    }
}
