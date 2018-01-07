package com.arbiter34.byml.nodes;

import com.arbiter34.byml.io.BinaryAccessFile;
import com.arbiter34.byml.util.NodeUtil;
import com.arbiter34.byml.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.nio.charset.Charset;
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
            entries.add(new String(buffer, 0, length -1, Charset.defaultCharset()));
        }

        return new StringTableNode(entries);
    }

    public void write(final BinaryAccessFile file) throws IOException {
        final int numEntries = entries.size();
        byte[] bytes = new byte[4];
        bytes[0] = (byte)NODE_TYPE;
        bytes[1] = (byte)(numEntries >>> 16);
        bytes[2] = (byte)(numEntries >>> 8);
        bytes[3] = (byte)(numEntries);
        file.write(bytes);
        long endOfOffsets = 0x04 + (4 * numEntries) + 0x04; // type/numEntries + 4bytes/offset
        if ((endOfOffsets % 4) != 0) {
            endOfOffsets += 4 - (endOfOffsets % 4);
        }
        for (int i = 0; i < numEntries; i++) {
            file.writeUnsignedInt(endOfOffsets);
            bytes = StringUtil.stringToAscii(entries.get(i));
            endOfOffsets += bytes.length;
        }
        file.writeUnsignedInt(endOfOffsets);
        for (int i = 0; i < numEntries; i++) {
            file.write(StringUtil.stringToAscii(entries.get(i)));
        }
        NodeUtil.byteAlign(file, true);
    }

    public List<String> getEntries() {
        return entries;
    }
}
