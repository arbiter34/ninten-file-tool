package com.arbiter34.file.util;

import com.arbiter34.file.io.BinaryAccessFile;

import java.io.IOException;

public class FileUtil {
    public static final int BYTE_ALIGNMENT = 4;

    public static void byteAlign(final BinaryAccessFile file, final boolean pad) throws IOException {
        byteAlign(file, BYTE_ALIGNMENT, pad);
    }

    public static void byteAlign(final BinaryAccessFile file, final long byteAlignment, final boolean pad) throws IOException {
        long position = file.getFilePointer();
        if ((position % byteAlignment) != 0) {
            int fromAlignment = (int)(byteAlignment - (position % byteAlignment));
            if (pad) {
                file.write(new byte[fromAlignment]);
            } else {
                file.skipBytes(fromAlignment);
            }
        }
    }
}
