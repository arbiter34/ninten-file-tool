package com.arbiter34.byml.serde;

import com.arbiter34.byml.nodes.ArrayNode;
import com.arbiter34.byml.nodes.BooleanNode;
import com.arbiter34.byml.nodes.DictionaryNode;
import com.arbiter34.byml.nodes.FloatNode;
import com.arbiter34.byml.nodes.HashNode;
import com.arbiter34.byml.nodes.IntegerNode;
import com.arbiter34.byml.nodes.Node;
import com.arbiter34.byml.nodes.StringNode;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class BymlNodeDeserializer extends JsonDeserializer<Node> {
    @Override
    public Node deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode jsonNode = p.getCodec().readTree(p);
        if (jsonNode.isArray()) {
            final ArrayNode arrayNode = new ArrayNode();
            for (JsonNode node : jsonNode) {
                arrayNode.add(p.getCodec().treeToValue(node, Node.class));
            }
            return arrayNode;
        } else if (jsonNode.isObject() && Optional.ofNullable(jsonNode.get("nodeType")).filter(n -> !n.asText().isEmpty()).isPresent()) {
            switch (jsonNode.get("nodeType").shortValue()) {
                case BooleanNode.NODE_TYPE:
                    return new BooleanNode(jsonNode.get("value").booleanValue());
                case FloatNode.NODE_TYPE:
                    return new FloatNode(jsonNode.get("value").floatValue());
                case HashNode.NODE_TYPE:
                    return new HashNode(jsonNode.get("value").longValue());
                case IntegerNode.NODE_TYPE:
                    return new IntegerNode(jsonNode.get("value").intValue());
                case StringNode.NODE_TYPE:
                    return new StringNode(jsonNode.get("value").asText());
                default:
                    throw new IOException(String.format("Invalid node type found: %s", jsonNode.get("nodeType").shortValue()));
            }
        } else if (jsonNode.isObject()) {
            final DictionaryNode dictionaryNode = new DictionaryNode();
            final Iterator<Entry<String, JsonNode>> it = jsonNode.fields();
            while (it.hasNext()) {
                final Map.Entry<String, JsonNode> entry = it.next();
                dictionaryNode.put(entry.getKey(), p.getCodec().treeToValue(entry.getValue(), Node.class));
            }
            return dictionaryNode;
        } else {
            throw new IOException(String.format("Found invalid node type: %s", jsonNode));
        }
    }
}
