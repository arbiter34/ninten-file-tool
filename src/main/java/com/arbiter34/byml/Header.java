package com.arbiter34.byml;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.arbiter34.byml.nodes.Node;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Header {
    public static final int MAGIC_BYTES = 0x4259;
    private static final List<Integer> validVersions = Stream.of(0x01, 0x02)
                                                           .collect(Collectors.toList());

    private final int magicBytes;
    private final int version;

    @JsonIgnore
    private final long nodeNameTableOffset;

    @JsonIgnore
    private final long stringValueTableOffset;

    @JsonIgnore
    private final long pathValueTableOffset;

    @JsonIgnore
    private final long rootNodeOffset;

    public Header(int magicBytes, int version, long nodeNameTableOffset, long stringValueTableOffset, long pathValueTableOffset,
                  long rootNodeOffset) {
        this.magicBytes = magicBytes;
        this.version = version;
        this.nodeNameTableOffset = nodeNameTableOffset;
        this.stringValueTableOffset = stringValueTableOffset;
        this.pathValueTableOffset = pathValueTableOffset;
        this.rootNodeOffset = rootNodeOffset;
    }

    @JsonCreator
    public Header(@JsonProperty("magicBytes") final int magicBytes, @JsonProperty("version") final int version) {
        this.magicBytes = magicBytes;
        this.version = version;
        this.nodeNameTableOffset = 0;
        this.stringValueTableOffset = 0;
        this.pathValueTableOffset = 0;
        this.rootNodeOffset = 0;
    }

    public static Header parse(final BinaryAccessFile file) throws IOException {
        if (file == null) {
            throw new UnsupportedOperationException("InputStream is not valid for parsing header.");
        }
        final int magicBytes = file.readShort();
        if (magicBytes != MAGIC_BYTES) {
            throw new IOException(String.format("Invalid magic bytes found. Found: %s Expected: %s", magicBytes, MAGIC_BYTES));
        }

        final int version = file.readShort();
        if (!validVersions.contains(version)) {
            throw new IOException(String.format("Invalid version found. Found: %s Expected: %s", version, validVersions));
        }

        final long nameTableOffset = file.readUnsignedInt();
        final long stringValueTableOffset = file.readUnsignedInt();
        //final long pathValueTableOffset = file.readUnsignedInt();
        final long rootNodeOffset = file.readUnsignedInt();
        return new Header(magicBytes, version, nameTableOffset, stringValueTableOffset, 0, rootNodeOffset);
    }

    public void write(final BinaryAccessFile outputStream) throws IOException {
        outputStream.writeShort(magicBytes);
        outputStream.writeShort(version);
        outputStream.writeUnsignedInt(nodeNameTableOffset);
        outputStream.writeUnsignedInt(stringValueTableOffset);
        outputStream.writeUnsignedInt(rootNodeOffset);
    }

    public int getVersion() {
        return version;
    }

    @JsonIgnore
    public int getSize() {
        switch (version) {
            case 1:
                return 20;
            case 2:
                return 16;
            default:
                throw new UnsupportedOperationException(String.format("Invalid version found. Found: %s Expected: %s", version, validVersions));
        }
    }

    public long getNodeNameTableOffset() {
        return nodeNameTableOffset;
    }

    public long getStringValueTableOffset() {
        return stringValueTableOffset;
    }

    public long getRootNodeOffset() {
        return rootNodeOffset;
    }

    public long getPathValueTableOffset() {
        return pathValueTableOffset;
    }
}
