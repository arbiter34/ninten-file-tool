package com.arbiter34.prod.serde;

import com.arbiter34.prod.Mesh;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.ArrayList;

public class MeshSerializer extends JsonSerializer<Mesh> {
    @Override
    public void serialize(Mesh value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeObjectField(value.getName(), new ArrayList<>(value));
        gen.writeEndObject();
    }
}
