package com.arbiter34.byml.nodes;

import com.arbiter34.file.io.BinaryAccessFile;
import com.arbiter34.byml.util.NodeUtil;
import com.arbiter34.byml.util.Pair;
import com.arbiter34.file.util.FileUtil;
import com.fasterxml.jackson.annotation.JsonGetter;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArrayNode extends ArrayList<Node> implements Node<List<Node>> {
    public static final short NODE_TYPE = 0xC0;

    private Long size;

    private static int count = 0;

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
        FileUtil.byteAlign(file, false);
        final ArrayNode instance = new ArrayNode();
        for (int i = 0; i < numEntries; i++) {
            final long value = file.readUnsignedInt();
            instance.add(NodeUtil.parseNode(nodeNameTable, stringValueTable, file, (short)nodeTypes[i], value));
        }
        return instance;
    }

    public long write(final Map<Node, Pair<Long, List<Long>>> nodeCache, final StringTableNode nodeNameTable,
                      final StringTableNode stringValueTable, final BinaryAccessFile file) throws IOException {
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
        FileUtil.byteAlign(file, true);
        for (final Node node : this) {
            if (node instanceof ArrayNode || node instanceof  DictionaryNode) {
                nodeCache.get(node).getRight().add(file.getFilePointer());
                file.write(new byte[4]);
            } else {
                NodeUtil.writeNode(nodeCache, nodeNameTable, stringValueTable, file, node);
            }
        }
        for (final Node node : this) {
            if ((node instanceof ArrayNode || node instanceof DictionaryNode) &&
                    nodeCache.get(node).getLeft() == null) {
                long offset = file.getFilePointer();
                NodeUtil.writeNode(nodeCache, nodeNameTable, stringValueTable, file, node);
                nodeCache.get(node).setLeft(offset);
            }
        }
        return file.getFilePointer();
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

    @Override
    public long getSize() {
        if (size == null) {
            size = 4 + size() + (4 * size()) + stream().map(Node::getSize).reduce((a, b) -> a + b).orElse(0L);
        }
        return size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayNode that = (ArrayNode) o;
        if (that.size() != size()) return false;
        return stream().allMatch(that::contains);
    }

    @Override
    public boolean hasChild(Node node) {
        return stream().anyMatch(n -> n.hasChild(node));
    }
}
