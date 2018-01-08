package com.arbiter34.prod;

import com.arbiter34.file.io.BinaryAccessFile;
import com.arbiter34.prod.serde.MeshDeserializer;
import com.arbiter34.prod.serde.MeshSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonSerialize(using = MeshSerializer.class)
@JsonDeserialize(using = MeshDeserializer.class)
public class Mesh extends ArrayList<MeshInstance> {

    private final String name;

    public Mesh(List<MeshInstance> instances, String name) {
        super();
        addAll(instances);
        this.name = name;
    }

    public static Mesh parse(final BinaryAccessFile file, final StringNameTable stringNameTable) throws IOException {
        final long size = file.readUnsignedInt();
        final long count = file.readUnsignedInt();
        final long nameOffset = file.readUnsignedInt();
        file.read(new byte[4]);

        final List<MeshInstance> instances = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            instances.add(MeshInstance.parse(file));
        }
        return new Mesh(instances, stringNameTable.getString((int)nameOffset));
    }

    public void write(final BinaryAccessFile file, final StringNameTable stringNameTable) throws IOException {
        final long size = size() * 32;
        final int count = size();
        final long nameOffset = stringNameTable.getOffset(name);

        file.writeUnsignedInt(size);
        file.writeUnsignedInt(count);
        file.writeUnsignedInt(nameOffset);
        file.write(new byte[4]);

        for (final MeshInstance instance : this) {
            instance.write(file);
        }
    }

    public String getName() {
        return name;
    }
}
