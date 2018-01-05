package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.arbiter34.byml.util.NodeUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DictionaryNode implements Node {
    public static final short NODE_TYPE = 0xC1;

    private final Map<String, Node> dictionary;

    public DictionaryNode(Map<String, Node> dictionary) {
        this.dictionary = dictionary;
    }

    public static DictionaryNode parse(final StringTableNode nodeNameTable, final StringTableNode stringValueTable,
                                       final BinaryAccessFile file) throws IOException {
        final long typeAndNumEntries = file.readUnsignedInt();
        if(((short)(typeAndNumEntries >>> 24) & 0x00FF) != NODE_TYPE) {
            throw new IOException(String.format("Invalid node type. Expected: %s Found: %s", NODE_TYPE, ((short)((typeAndNumEntries & 0xF000) >>> 24))));
        }
        final int numEntries = (int)(typeAndNumEntries & 0x00FFFFFF);

        final Map<String, Node> dictionary = new HashMap<>();
        for (int i = 0; i < numEntries; i++) {
            final long nameIndexAndType = file.readUnsignedInt();
            final int nameIndex = (int)(nameIndexAndType >>> 8);
            final short nodeType = (short)(nameIndexAndType & 0x000000FF);
            final long value = file.readUnsignedInt();
            final String key = nodeNameTable.getEntries().get(nameIndex);
            final Node node = NodeUtil.parseNode(nodeNameTable, stringValueTable, file, nodeType, value);
            dictionary.put(key, node);
        }
        return new DictionaryNode(dictionary);
    }
}
