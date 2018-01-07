package com.arbiter34.byml.util;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.arbiter34.byml.nodes.ArrayNode;
import com.arbiter34.byml.nodes.BooleanNode;
import com.arbiter34.byml.nodes.DictionaryNode;
import com.arbiter34.byml.nodes.FloatNode;
import com.arbiter34.byml.nodes.HashNode;
import com.arbiter34.byml.nodes.IntegerNode;
import com.arbiter34.byml.nodes.Node;
import com.arbiter34.byml.nodes.StringNode;
import com.arbiter34.byml.nodes.StringTableNode;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NodeUtil {
    public static final int BYTE_ALIGNMENT = 4;

    public static Node parseNode(final StringTableNode nodeNameTable, final StringTableNode stringValueTable,
                                 final BinaryAccessFile file, final short nodeType, final long value) throws IOException {
        Long position;
        Node node;
        switch (nodeType) {
            case ArrayNode.NODE_TYPE:
                position = null;
                if (value != 0) {
                    position = file.getFilePointer();
                    file.seek(value);
                }
                node = ArrayNode.parse(nodeNameTable, stringValueTable, file);
                if (position != null) {
                    file.seek(position);
                }
                return node;
            case BooleanNode.NODE_TYPE:
                return BooleanNode.parse(value);
            case DictionaryNode.NODE_TYPE:
                position = null;
                if (value != 0) {
                    position = file.getFilePointer();
                    file.seek(value);
                }
                node = DictionaryNode.parse(nodeNameTable, stringValueTable, file);
                if (position != null) {
                    file.seek(position);
                }
                return node;
            case FloatNode.NODE_TYPE:
                return FloatNode.parse(value);
            case HashNode.NODE_TYPE:
                return HashNode.parse(value);
            case IntegerNode.NODE_TYPE:
                return IntegerNode.parse(value);
            case StringNode.NODE_TYPE:
                return StringNode.parse(stringValueTable, value);
            default:
                throw new IOException(String.format("Found invalid node type: %s", nodeType));
        }
    }

    public static void writeNode(final Map<Node, Pair<Long, List<Long>>> nodeCache, final StringTableNode nodeNameTable,
                                 final StringTableNode stringValueTable,
                                 final BinaryAccessFile file, final Node node) throws IOException {
        if (node instanceof ArrayNode) {
            ArrayNode.class.cast(node).write(nodeCache, nodeNameTable, stringValueTable, file);
        } else if (node instanceof BooleanNode) {
            BooleanNode.class.cast(node).write(file);
        } else if (node instanceof  DictionaryNode) {
            DictionaryNode.class.cast(node).write(nodeCache, nodeNameTable, stringValueTable, file);
        } else if (node instanceof FloatNode) {
            FloatNode.class.cast(node).write(file);
        } else if (node instanceof HashNode) {
            HashNode.class.cast(node).write(file);
        } else if (node instanceof IntegerNode) {
            IntegerNode.class.cast(node).write(file);
        } else if (node instanceof StringNode) {
            StringNode.class.cast(node).write(file, stringValueTable);
        } else {
            throw new IOException(String.format("Found invalid node type: %s", node.getClass()));
        }
    }

    public static void byteAlign(final BinaryAccessFile file, final boolean pad) throws IOException {
        long position = file.getFilePointer();
        if ((position % BYTE_ALIGNMENT) != 0) {
            int fromAlignment = (int)(BYTE_ALIGNMENT - (position % BYTE_ALIGNMENT));
            if (pad) {
                file.write(new byte[fromAlignment]);
            } else {
                file.skipBytes(fromAlignment);
            }
        }
    }

    public static List<Node> sortForPadding(long offset, final List<Node> nodes) {
        return nodes;
    }
}
