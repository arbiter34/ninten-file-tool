package com.arbiter34.yaz0;

import com.arbiter34.file.io.BinaryAccessFile;

import java.io.IOException;
import java.util.Arrays;

public class Yaz0Encoder {
    private static final long HEADER_SIZE = 0x10;
    private static final int MAX_SEARCH_SIZE = 0x111;
    private static final int REQUIRES_THIRD_BYTE = 0x12;

    public static BinaryAccessFile encode(final BinaryAccessFile in, final String outPath) throws IOException {
        final BinaryAccessFile out = new BinaryAccessFile(outPath, "rw");

        int headerPos = 8;
        long headerPointer = 0;
        short header = 0;
        while (in.getFilePointer() < in.length()) {
            if (headerPos == 8) {
                // mark current end of out
                long outMark = out.getFilePointer();
                // seek to current header
                out.seek(headerPointer);
                // write header
                out.writeByte(header);
                // seek to current end of out
                out.seek(outMark);

                // reset header and header pos and mark location
                header = 0;
                headerPointer = out.getFilePointer();
                headerPos = 0;
            }
            int searchSize = MAX_SEARCH_SIZE;
            long searchStart = out.getFilePointer();
            boolean found = false;
            while (searchSize > 0) {
                if (searchSize > searchStart) {
                    searchSize--;
                    continue;
                }
                byte[] searchBytes = new byte[searchSize];
                in.read(searchBytes);
                long offsetFound = offsetOf(out, searchBytes);
                if (offsetFound == -1) {
                    searchSize--;
                    in.seek(searchStart);
                    continue;
                }
                found = true;
                int backReference = (int)(offsetFound - out.getFilePointer()) - 1;
                headerPos++;    // 0 bit in header for reference byte
                if (searchSize < REQUIRES_THIRD_BYTE) {
                    int toWrite = ((searchSize - 0x02) << 12) | backReference;
                    out.writeShort(toWrite);
                } else {
                    out.writeShort(backReference);
                    out.write(searchSize);
                }
                break;
            }
            if (!found) {
                out.write(in.read());
                header |= (0x80 >>> headerPos);
                headerPos++;
            }
        }
        return out;
    }

    private static long offsetOf(final BinaryAccessFile file, byte[] bytes) throws IOException {
        long currentPosition = file.getFilePointer();
        file.seek(file.getFilePointer() - bytes.length);
        final byte[] searchBytes = new byte[bytes.length];
        while (file.getFilePointer() > currentPosition) {
            long mark = file.getFilePointer();
            file.read(searchBytes);
            if (Arrays.equals(bytes, searchBytes)) {
                file.seek(currentPosition);
                return mark;
            }
            file.seek(mark - bytes.length);
        }
        file.seek(currentPosition);
        return -1;
    }
}
