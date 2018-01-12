package com.arbiter34.byml;

import com.arbiter34.byml.nodes.ArrayNode;
import com.arbiter34.byml.nodes.DictionaryNode;
import com.arbiter34.byml.nodes.Node;
import com.arbiter34.byml.nodes.PathTableNode;
import com.arbiter34.byml.nodes.StringNode;
import com.arbiter34.byml.nodes.StringTableNode;
import com.arbiter34.byml.util.NodeUtil;
import com.arbiter34.byml.util.Pair;
import com.arbiter34.file.io.BinaryAccessFile;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BymlFile {
    public static final int MAGIC_BYTES = 0x4259;
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                                                                       .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @JsonProperty("header")
    private Header header;

    @JsonProperty("root")
    private final Node root;

    @JsonIgnore
    private StringTableNode nodeNameTable;

    @JsonIgnore
    private StringTableNode stringNameTable;

    @JsonIgnore
    private final PathTableNode pathTable;

    @JsonCreator
    public BymlFile(@JsonProperty("header") Header header, @JsonProperty("root") Node root,
                    @JsonProperty("nodeNameTable") StringTableNode nodeNameTable,
                    @JsonProperty("stringNameTable") StringTableNode stringNameTable) {
        this.header = header;
        this.root = root;
        this.nodeNameTable = nodeNameTable;
        this.stringNameTable = stringNameTable;
        this.pathTable = null;
    }

   public Node getRoot() {
        return root;
   }

    public static BymlFile parse(final String path) throws IOException {
        try (final BinaryAccessFile file = new BinaryAccessFile(path, "r")) {
            return parse(file);
        }
    }

   public static BymlFile parse(final BinaryAccessFile file) throws IOException {
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
       short nodeType = (short) (0x00FF & file.readByte());
       if (nodeType != DictionaryNode.NODE_TYPE && nodeType != ArrayNode.NODE_TYPE) {
           throw new IOException(String.format("Invalid node type found. Expected: (%s|%s) Found: %s",
                   DictionaryNode.NODE_TYPE,
                   ArrayNode.NODE_TYPE,
                   nodeType));
       }
       file.seek(header.getRootNodeOffset());
       final Node root = NodeUtil.parseNode(nodeNameTable, stringNameTable, file, nodeType, 0l);
       return new BymlFile(header, root, nodeNameTable, stringNameTable);
   }

    public void write(final String path) throws IOException {
        try (final BinaryAccessFile file = new BinaryAccessFile(path, "rw")) {
            // We need to calc some offsets before writing the header
            final long headerSize = header.getSize();
            final long nodeNameTableOffset = headerSize;

            // Build new NodeNameTable and StringValueTable
            nodeNameTable = new StringTableNode(buildNodeNameTable(new ArrayList<>(), root).stream().sorted().collect(Collectors.toList()));
            stringNameTable = new StringTableNode(buildStringValueTable(new ArrayList<>(), root).stream().sorted().collect(Collectors.toList()));

            file.seek(nodeNameTableOffset);
            nodeNameTable.write(file);

            final long stringNameTableOffset = file.getFilePointer();
            stringNameTable.write(file);

            final long rootNodeOffset = file.getFilePointer();
            header = new Header(MAGIC_BYTES, header.getVersion(), nodeNameTableOffset, stringNameTableOffset, 0, rootNodeOffset);
            file.seek(0);
            header.write(file);

            file.seek(rootNodeOffset);

            final Map<Node, Pair<Long, List<Long>>> nodeCache = buildNodeCache(root, new LinkedHashMap<>());
            NodeUtil.writeNode(nodeCache, nodeNameTable, stringNameTable, file, root);
            writeNodeCacheOffsets(file, nodeCache);
        }
    }

    public String toJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }

    private void writeNodeCacheOffsets(final BinaryAccessFile file, final Map<Node, Pair<Long, List<Long>>> nodeCache) throws IOException {
        for (final Pair<Long, List<Long>> offSetPair : nodeCache.values()) {
            for (final Long headerOffset : offSetPair.getRight()) {
                file.seek(headerOffset);
                file.writeUnsignedInt(offSetPair.getLeft());
            }
        }
    }

    private Map<Node,Pair<Long,List<Long>>> buildNodeCache(Node root, Map<Node, Pair<Long, List<Long>>> nodeCache) {
        if (root instanceof ArrayNode && !nodeCache.containsKey(root)) {
            nodeCache.put(root, Pair.of(null, new ArrayList<>()));
            for (final Node node : (ArrayNode)root) {
                buildNodeCache(node, nodeCache);
            }
        } else if (root instanceof DictionaryNode && !nodeCache.containsKey(root)) {
            nodeCache.put(root, Pair.of(null, new ArrayList<>()));
            for (final Node node : ((DictionaryNode)root).values()) {
                buildNodeCache(node, nodeCache);
            }
        }
        return nodeCache;
    }

    public int countType(Node root, Class<? extends Node> ... classes) {
        int total = 0;
        if (Stream.of(classes).anyMatch(clazz -> clazz.equals(root.getClass()))) {
            total += 1;
        }
        if (root.getClass().equals(DictionaryNode.class)) {
            for (Node node : DictionaryNode.class.cast(root).values()) {
                total += countType(node, classes);
            }
        } else if (root.getClass().equals(ArrayNode.class)) {
            for (Node node : ArrayNode.class.cast(root)) {
                total += countType(node, classes);
            }
        }
        return total;
    }

    private static List<String> buildStringValueTable(final List<String> names, Node root) {
        if (root instanceof StringNode) {
            final StringNode node = (StringNode)root;
            if (!names.contains(node.getValue())) {
                names.add(node.getValue());
            }
        } else if (root instanceof ArrayNode) {
            final ArrayNode array = (ArrayNode)root;
            for (final Node node : array) {
                buildStringValueTable(names, node);
            }
        } else if (root instanceof DictionaryNode) {
            final DictionaryNode dictionaryNode = (DictionaryNode)root;
            for (final Node node : dictionaryNode.values()) {
                buildStringValueTable(names, node);
            }
        }
        return names;
    }

    private static List<String> buildNodeNameTable(final List<String> names, Node root) {
        if (root instanceof ArrayNode) {
            final ArrayNode array = (ArrayNode)root;
            for (final Node node : array) {
                buildNodeNameTable(names, node);
            }
        } else if (root instanceof DictionaryNode) {
            final DictionaryNode dictionaryNode = (DictionaryNode)root;
            for (final Map.Entry<String, Node> entry : dictionaryNode.entrySet()) {
                if (!names.contains(entry.getKey())) {
                    names.add(entry.getKey());
                }
                buildNodeNameTable(names, entry.getValue());
            }
        }
        return names;
    }

    public static BymlFile fromJson(final String json) throws IOException {
        return objectMapper.readValue(json, BymlFile.class);
    }

    public static BymlFile fromJson(final byte[] json) throws IOException {
        return objectMapper.readValue(json, BymlFile.class);
    }
}
