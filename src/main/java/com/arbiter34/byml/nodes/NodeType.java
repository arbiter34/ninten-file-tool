package com.arbiter34.byml.nodes;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum NodeType {
    ARRAY(ArrayNode.NODE_TYPE, ArrayNode.class),
    BOOLEAN(BooleanNode.NODE_TYPE, BooleanNode.class),
    DICTIONARY(DictionaryNode.NODE_TYPE, DictionaryNode.class),
    FLOAT(FloatNode.NODE_TYPE, FloatNode.class),
    HASH(HashNode.NODE_TYPE, HashNode.class),
    INTEGER(IntegerNode.NODE_TYPE, IntegerNode.class),
    STRING(StringNode.NODE_TYPE, StringNode.class);

    private static final Map<Short, NodeType> nodeTypeMap = Stream.of(values())
                                                                  .collect(Collectors.toMap(NodeType::getNodeType, Function.identity()));

    private static final Map<Class<? extends Node>, NodeType> clazzMap = Stream.of(values())
                                                                               .collect(Collectors.toMap(NodeType::getClazz, Function.identity()));

    private final short nodeType;
    private final Class<? extends Node> clazz;

    public short getNodeType() {
        return nodeType;
    }

    public Class<? extends Node> getClazz() {
        return clazz;
    }

    NodeType(short nodeType, Class<? extends Node> clazz) {
        this.nodeType = nodeType;
        this.clazz = clazz;
    }

    public static NodeType valueOfNodeType(final short nodeType) {
        return nodeTypeMap.get(nodeType);
    }

    public static NodeType valueOfClazz(final Class<? extends Node> clazz) {
        return Optional.ofNullable(clazz).map(clazzMap::get).orElse(null);
    }
}
