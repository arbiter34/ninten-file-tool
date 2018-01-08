package com.arbiter34.prod.serde;

import com.arbiter34.prod.ProdFile;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.ArrayList;

public class ProdFileSerializer extends JsonSerializer<ProdFile> {
    @Override
    public void serialize(ProdFile value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
        gen.writeStartObject();
        gen.writeObjectField("header", value.getHeader());
        gen.writeObjectField("meshes", new ArrayList<>(value));
        gen.writeEndObject();
    }
}
