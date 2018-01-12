package com.arbiter34.prod;

import com.arbiter34.file.io.BinaryAccessFile;
import com.arbiter34.prod.serde.ProdFileDeserializer;
import com.arbiter34.prod.serde.ProdFileSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonSerialize(using = ProdFileSerializer.class)
@JsonDeserialize(using = ProdFileDeserializer.class)
public class ProdFile extends ArrayList<Mesh> {
    public static final long MAGIC_BYTES = 0x50724F44; //PrOD
    private static final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private Header header;

    @JsonIgnore
    private StringNameTable stringNameTable;

    public ProdFile(final Header header, final StringNameTable stringNameTable,
                    final List<Mesh> meshes) {
        super();
        addAll(meshes);
        this.header = header;
        this.stringNameTable = stringNameTable;
    }

    public ProdFile(final Header header, final List<Mesh> meshes) {
        super();
        addAll(meshes);
        this.header = header;
    }

    public static ProdFile parse(final String path) throws IOException {
        try (final BinaryAccessFile file = new BinaryAccessFile(path, "r")) {
            return parse(file);
        }
    }

    public static ProdFile parse(final BinaryAccessFile file) throws IOException {
        final Header header = Header.parse(file);
        final long currentPosition = file.getFilePointer();
        file.seek(header.getStringTableOffset());
        final StringNameTable stringNameTable = StringNameTable.parse(file);
        file.seek(currentPosition);

        final List<Mesh> meshes = new ArrayList<>();
        for (int i = 0; i < header.getNumMeshes(); i++) {
            meshes.add(Mesh.parse(file, stringNameTable));
        }
        return new ProdFile(header, stringNameTable, meshes);
    }

    public void write(final String path) throws IOException {
            try (final BinaryAccessFile file = new BinaryAccessFile(path, "rw")) {
                stringNameTable = new StringNameTable(stream().map(Mesh::getName)
                        .collect(Collectors.toList()));
                // Write meshes first
                file.seek(0x20);
                for (final Mesh mesh : this) {
                    mesh.write(file, stringNameTable);
                }
                final long stringNameTableOffset = file.getFilePointer();
                stringNameTable.write(file);
                final long fileSize = file.getFilePointer();

                header = new Header(header.getVersion(), header.getAlways1(), header.getUnknown(), fileSize,
                        size(), stringNameTableOffset);

                file.seek(0);
                header.write(file);
            }
    }

    public String toJson() throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }

    public static ProdFile fromJson(final String json) throws IOException {
        return objectMapper.readValue(json, ProdFile.class);
    }

    public static ProdFile fromJson(final byte[] json) throws IOException {
        return objectMapper.readValue(json, ProdFile.class);
    }

    public Header getHeader() {
        return header;
    }
}
