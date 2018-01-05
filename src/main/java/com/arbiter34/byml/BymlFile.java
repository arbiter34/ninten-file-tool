package com.arbiter34.byml;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.arbiter34.byml.nodes.ArrayNode;
import com.arbiter34.byml.nodes.DictionaryNode;
import com.arbiter34.byml.nodes.Node;
import com.arbiter34.byml.nodes.PathTableNode;
import com.arbiter34.byml.nodes.StringTableNode;
import com.arbiter34.byml.util.NodeUtil;

import java.io.IOException;

public class BymlFile {

    private Header header;
    private final Node root;
    private final StringTableNode nodeNameTable;
    private final StringTableNode stringNameTable;
    private final PathTableNode pathTable;

    public BymlFile(Header header, Node root, StringTableNode nodeNameTable, StringTableNode stringNameTable,
                    PathTableNode pathTable) {
        this.header = header;
        this.root = root;
        this.nodeNameTable = nodeNameTable;
        this.stringNameTable = stringNameTable;
        this.pathTable = pathTable;
    }

    public static BymlFile parse(final String path) throws IOException {
        final BinaryAccessFile file = new BinaryAccessFile(path, "r");
        final Header header = Header.parse(file);

        StringTableNode nodeNameTable = null;
        if (header.getNodeNameTableOffset() != 0) {
            file.seek(header.getNodeNameTableOffset());
            nodeNameTable = StringTableNode.parse(header.getNodeNameTableOffset(), file);
        }
        StringTableNode stringNameTable = null;
        if (header.getStringValueTableOffset() != 0) {
            file.seek(header.getStringValueTableOffset());
            stringNameTable = StringTableNode.parse(header.getStringValueTableOffset(), file);
        }

        PathTableNode pathTable = null;
        if (header.getPathValueTableOffset() != 0) {
            file.seek(header.getPathValueTableOffset());
            pathTable = PathTableNode.parse(header.getPathValueTableOffset(), file);
        }

        file.seek(header.getRootNodeOffset());
        short nodeType = (short)(0x00FF & file.readByte());
        if (nodeType != DictionaryNode.NODE_TYPE && nodeType != ArrayNode.NODE_TYPE) {
            throw new IOException(String.format("Invalid node type found. Expected: (%s|%s) Found: %s",
                                                DictionaryNode.NODE_TYPE,
                                                ArrayNode.NODE_TYPE,
                                                nodeType));
        }
        file.seek(header.getRootNodeOffset());
        final Node root = NodeUtil.parseNode(nodeNameTable, stringNameTable, file, nodeType, 0l);
        return new BymlFile(header, root, nodeNameTable, stringNameTable, pathTable);
    }

    public void write(final String path) throws IOException {
        final BinaryAccessFile file = new BinaryAccessFile(path, "rw");

        // We need to calc some offsets before writing the header
        final long headerSize = header.getSize();
        final long nodeNameTableOffset = headerSize;

        file.seek(nodeNameTableOffset);
        nodeNameTable.write(file);

        final long stringNameTableOffset = file.getFilePointer();
        stringNameTable.write(file);

        final long rootNodeOffset = file.getFilePointer();
        header = new Header(Header.MAGIC_BYTES, header.getVersion(), nodeNameTableOffset, stringNameTableOffset, 0, rootNodeOffset);
        file.seek(0);
        header.write(file);

        file.seek(rootNodeOffset);
        NodeUtil.writeNode(nodeNameTable, stringNameTable, file, root);
    }
}
