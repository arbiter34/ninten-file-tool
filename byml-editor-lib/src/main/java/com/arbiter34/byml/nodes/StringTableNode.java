package com.arbiter34.byml.nodes;

import com.arbiter34.file.io.BinaryAccessFile;
import com.arbiter34.file.util.StringUtil;
import com.arbiter34.file.util.FileUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StringTableNode {
    private static final short NODE_TYPE = 0xC2;

    private final List<String> entries;

    @JsonCreator
    public StringTableNode(@JsonProperty("entries") List<String> entries) {
        this.entries = entries;
    }

    public static StringTableNode parse(final long offset, final BinaryAccessFile inputStream) throws IOException {
        final long typeAndNumEntries = inputStream.readUnsignedInt();
        final short nodeType = (short)(0x0000000000FF & (typeAndNumEntries >>> 24));
        if (nodeType != NODE_TYPE) {
            throw new IOException(String.format("Found invalid node type. Expected: %s Found: %s", NODE_TYPE, nodeType));
        }

        final int numEntries = (int)(0x00FFFFFF & typeAndNumEntries);
        final long[] entryOffsets = new long[numEntries+1];
        for (int i = 0 ; i < entryOffsets.length; i++) {
            entryOffsets[i] = inputStream.readUnsignedInt();
        }

        final List<String> entries = new ArrayList<>();
        for (int i = 0; i < numEntries; i++) {
            long currentOffset = entryOffsets[i];
            long nextOffset = entryOffsets[i+1];
            int length = (int)(nextOffset - currentOffset);
            byte[] buffer = new byte[length];
            int bytesRead = inputStream.read(buffer);
            if (bytesRead != length) {
                throw new IOException(String.format("Found invalid number of bytes to read. Expected: %s Found", length, bytesRead));
            }
            entries.add(new String(buffer, 0, length -1, StandardCharsets.UTF_8));
        }

        return new StringTableNode(entries);
    }

    public void write(final BinaryAccessFile file) throws IOException {
        long startOfStringTable = file.getFilePointer();
        final int numEntries = entries.size();

        // Build and write header
        byte[] bytes = new byte[4];
        bytes[0] = (byte)NODE_TYPE;
        bytes[1] = (byte)(numEntries >>> 16);
        bytes[2] = (byte)(numEntries >>> 8);
        bytes[3] = (byte)(numEntries);
        file.write(bytes);
        final List<Long> offsets = new ArrayList<>();

        // Mark start and end of offsets
        long startOfOffsets = file.getFilePointer();
        long endOfOffsets = startOfOffsets + (4 * numEntries) + 0x04; // type/numEntries + 4bytes/offset + end of last string offset

        // 4 byte align end of offsets
        if ((endOfOffsets % 4) != 0) {
            endOfOffsets += 4 - (endOfOffsets % 4);
        }

        // Seek to string start
        file.seek(endOfOffsets);
        for (int i = 0; i < numEntries; i++) {
            // Add offset of string start, then write string as utf-8 bytes null terminated
            offsets.add(file.getFilePointer() - startOfStringTable);
            file.write(entries.get(i).getBytes(StandardCharsets.UTF_8));
            file.write((byte)0);
        }
        // Add offset to end of last string
        offsets.add(file.getFilePointer() - startOfStringTable);

        // Mark end of table for prologue
        final long endOfStringTable = file.getFilePointer();

        // Seek and write all offsets
        file.seek(startOfOffsets);
        for (final Long offset : offsets) {
            file.writeUnsignedInt(offset);
        }

        // Prologue - seek to end and 4-byte align
        file.seek(endOfStringTable);
        FileUtil.byteAlign(file, true);
    }

    public List<String> getEntries() {
        return entries;
    }
}
