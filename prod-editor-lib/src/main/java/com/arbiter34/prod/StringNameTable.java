package com.arbiter34.prod;

import com.arbiter34.file.io.BinaryAccessFile;
import com.arbiter34.file.util.FileUtil;
import com.arbiter34.file.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StringNameTable extends ArrayList<String> {
    private static final int BYTE_ALIGNMENT = 8;

    public StringNameTable(List<String> strings) {
        super();
        addAll(strings);
    }

    public static StringNameTable parse(final BinaryAccessFile file) throws IOException {
        final long numStrings = file.readUnsignedInt();
        final long tableSize = file.readUnsignedInt();
        final long start = file.getFilePointer();
        final List<String> strings = new ArrayList<>();

        List<Byte> buffer = new ArrayList<>();
        while (file.getFilePointer() - start < tableSize) {
            final byte c = file.readByte();
            if (c == '\0') {
                final byte[] bytes = new byte[buffer.size()];
                for (int j = 0; j < bytes.length; j++) {
                    bytes[j] = buffer.get(j);
                }
                strings.add(new String(bytes));
                buffer.clear();
                byteAlign(file, false);
                continue;
            }
            buffer.add(c);
        }
        assert((file.getFilePointer() - start) == tableSize);
        assert(strings.size() == numStrings);
        return new StringNameTable(strings);
    }

    public void write(final BinaryAccessFile file) throws IOException {
        file.writeUnsignedInt(size());
        file.write(new byte[4]);
        final long start = file.getFilePointer();
        for (final String string : this) {
            file.write(StringUtil.stringToAscii(string));
            byteAlign(file, true);
        }
        final long currentPosition = file.getFilePointer();
        final long size = currentPosition - start;
        file.seek(start - 4 );
        file.writeUnsignedInt(size);
        file.seek(currentPosition);
    }

    public String getString(final long offset) {
        if (offset < 0) {
            return null;
        }
        for (final String string : this) {
            if (getOffset(string) == offset) {
                return string;
            }
        }
        return null;
    }

    public long getOffset(final String name) {
        if (!contains(name)) {
            return -1;
        }
        final List<String> before = subList(0, indexOf(name));
        return 8 + before.stream().map(this::getLength).reduce((a, b) -> a + b).orElse(0);
    }

    private int getLength(final String string) {
        int length = StringUtil.stringToAscii(string).length;
        length += 4 - (length % 4);
        return length;
    }

    private static void byteAlign(final BinaryAccessFile file, final boolean pad) throws IOException {
        final long size = 4 - (file.getFilePointer() % 4);
        if (pad) {
            file.write(new byte[(int)size]);
        } else {
            file.skipBytes((int)size);
        }
    }
}
