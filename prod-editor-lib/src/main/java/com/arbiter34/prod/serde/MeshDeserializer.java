package com.arbiter34.prod.serde;

import com.arbiter34.prod.Mesh;
import com.arbiter34.prod.MeshInstance;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MeshDeserializer extends JsonDeserializer<Mesh> {
    @Override
    public Mesh deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode jsonNode = p.getCodec().readTree(p);

        if (jsonNode.size() != 1) {
            throw new IOException("Unable to parse Mesh json, expected only one field for name.");
        }

        final Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields();
        while (it.hasNext()) {
            final Map.Entry<String, JsonNode> entry = it.next();
            final List<MeshInstance> instances = new ArrayList<>();
            for (final JsonNode node : entry.getValue()) {
                instances.add(p.getCodec().treeToValue(node, MeshInstance.class));
            }
            return new Mesh(instances, entry.getKey());
        }
        throw new IOException("Unable to parse Mesh json, expected one and only one entry for mesh name.");
    }
}
