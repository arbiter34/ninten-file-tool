package com.arbiter34.prod.serde;

import com.arbiter34.prod.Header;
import com.arbiter34.prod.Mesh;
import com.arbiter34.prod.ProdFile;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProdFileDeserializer extends JsonDeserializer<ProdFile> {
    @Override
    public ProdFile deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode jsonNode = p.getCodec().readTree(p);

        if (!jsonNode.has("meshes") || !jsonNode.get("meshes").isArray() || !jsonNode.has("header")) {
            throw new IOException("Error deserializing ProdFile json, missing header or meshes.");
        }

        final Header header = p.getCodec().treeToValue(jsonNode.get("header"), Header.class);

        final List<Mesh> meshes = new ArrayList<>();
        for (final JsonNode mesh : jsonNode.get("meshes")) {
            meshes.add(p.getCodec().treeToValue(mesh, Mesh.class));
        }
        return new ProdFile(header, meshes);
    }
}
