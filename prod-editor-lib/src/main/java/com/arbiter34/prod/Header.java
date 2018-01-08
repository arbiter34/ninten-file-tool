package com.arbiter34.prod;

import com.arbiter34.file.io.BinaryAccessFile;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.arbiter34.prod.ProdFile.MAGIC_BYTES;

public class Header {
    private static final List<Short> validVersions = Arrays.asList((short)1);

    private final short version;

    @JsonIgnore
    private final long always1;

    private final long unknown;

    @JsonIgnore
    private final long fileSize;

    @JsonIgnore
    private final long numMeshes;

    @JsonIgnore
    private final long stringTableOffset;

    public Header(short version, long always1, long unknown, long fileSize,
                  long numMeshes, long stringTableOffset) {
        this.version = version;
        this.always1 = always1;
        this.unknown = unknown;
        this.fileSize = fileSize;
        this.numMeshes = numMeshes;
        this.stringTableOffset = stringTableOffset;
    }

    @JsonCreator
    public Header(@JsonProperty("version") short version, @JsonProperty("unknown") long unknown) {
        this.version = version;
        this.always1 = 1;
        this.unknown = unknown;
        this.fileSize = -1;
        this.numMeshes = -1;
        this.stringTableOffset = -1;
    }

    public static Header parse(final BinaryAccessFile file) throws IOException {
        if (file == null) {
            throw new UnsupportedOperationException("InputStream is not valid for parsing header.");
        }
        final long magicBytes = file.readUnsignedInt();
        if (magicBytes != MAGIC_BYTES) {
            throw new IOException(String.format("Invalid magic bytes found. Found: %s Expected: %s", magicBytes, MAGIC_BYTES));
        }

        final short version = file.readByte();
        if (!validVersions.contains(version)) {
            throw new IOException(String.format("Invalid version found. Found: %s Expected: %s", version, validVersions));
        }
        file.read(new byte[3]);

        final long always1 = file.readUnsignedInt();
        final long unknown = file.readUnsignedInt();
        final long fileSize = file.readUnsignedInt();
        final long numMeshes = file.readUnsignedInt();
        final long stringTableOffset = file.readUnsignedInt();
        file.read(new byte[4]);
        return new Header(version, always1, unknown, fileSize, numMeshes, stringTableOffset);
    }

    public void write(final BinaryAccessFile file) throws IOException {
        file.writeUnsignedInt(MAGIC_BYTES);
        file.writeByte(version);
        file.write(new byte[3]);
        file.writeUnsignedInt(always1);
        file.writeUnsignedInt(unknown);
        file.writeUnsignedInt(fileSize);
        file.writeUnsignedInt(numMeshes);
        file.writeUnsignedInt(stringTableOffset);
        file.write(new byte[4]);
    }

    public short getVersion() {
        return version;
    }

    public long getAlways1() {
        return always1;
    }

    public long getUnknown() {
        return unknown;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getNumMeshes() {
        return numMeshes;
    }

    public long getStringTableOffset() {
        return stringTableOffset;
    }
}
