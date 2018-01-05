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
import java.util.Optional;

public class NodeUtil {

    public static Node parseNode(final StringTableNode nodeNameTable, final StringTableNode stringValueTable,
                                 final BinaryAccessFile file, final short nodeType, final long value) throws IOException {
        Long position = null;
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
}
