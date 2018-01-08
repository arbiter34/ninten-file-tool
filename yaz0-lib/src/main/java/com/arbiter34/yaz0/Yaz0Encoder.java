package com.arbiter34.yaz0;

import com.arbiter34.byml.util.Pair;
import com.arbiter34.file.io.BinaryAccessFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Yaz0Encoder {
    private static final long HEADER_SIZE = 0x10;
    private static final int MAX_SEARCH_SIZE = 0x111;
    private static final int REQUIRES_THIRD_BYTE = 0x12;

    public static BinaryAccessFile encode(final BinaryAccessFile in, final String outPath) throws IOException {
        final BinaryAccessFile out = new BinaryAccessFile(outPath, "rw");
        out.writeUnsignedInt(Yaz0Decoder.MAGIC_BYTES);
        out.writeUnsignedInt(in.length());
        out.write(new byte[8]);

        int headerPos = 0;
        long headerPointer = out.getFilePointer();
        short header = 0;
        out.seek(headerPointer + 1);
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
                out.seek(headerPointer + 1);
            }
            int searchSize = MAX_SEARCH_SIZE;
            long searchStart = in.getFilePointer();
            boolean found = false;
            byte[] searchBytes = new byte[searchSize];
            in.read(searchBytes);
            in.seek(in.getFilePointer() - searchSize);
            Pair<Long, Integer> offsetBestPair = offsetOf(out, searchBytes);
            if (offsetBestPair.getLeft() == -1) {
                // No bytes matched -> seek search start -> read byte -> write -> set header bit
                out.write(in.read());
                header |= (0x80 >>> headerPos);
                headerPos++;
                continue;
            }

            final int best = offsetBestPair.getRight();
            if (best > 10) {
                int x = best;
            }
            final long offset = offsetBestPair.getLeft();
            // We found bytes -> seek input over matched bytes
            in.seek(searchStart + best);
            int backReference = (int)(out.getFilePointer() - offset + 2 - 1);
            headerPos++;    // 0 bit in header for reference byte
            if (best < REQUIRES_THIRD_BYTE) {
                int toWrite = ((searchSize - 0x02) << 12) | backReference;
                out.writeShort(toWrite);
            } else {
                out.writeShort(backReference);
                out.write(searchSize);
            }
            headerPos++;
        }

        return out;
    }

    private static Pair<Long, Integer> offsetOf(final BinaryAccessFile file, byte[] searchBytes) throws IOException {
        long currentPosition = file.getFilePointer();
        if (currentPosition - searchBytes.length < HEADER_SIZE) {
            file.seek(currentPosition);
            return Pair.of(-1L, -1);
        }

        byte[] buffer = new byte[searchBytes.length];
        Pair<Long, Integer> longest = Pair.of(-1L, -1);
        while (currentPosition - file.getFilePointer() < MAX_SEARCH_SIZE) {
            long mark = file.getFilePointer();
            file.read(buffer);
            longest = longestSubarray(searchBytes, buffer);
            if (longest.getLeft() != -1) {
                byte[] found = Arrays.copyOf(searchBytes, longest.getRight());
                long offset = currentPosition + longest.getLeft();
                offsetCache.put(Arrays.hashCode(found), offset);
                break;
            }
            file.seek(mark - searchBytes.length);
        }
        file.seek(currentPosition);
        return longest;
    }

    private static Map<Integer, Long> offsetCache = new HashMap<>();

    public static Pair<Long, Integer> longestSubarray(byte[] source, byte[] target) {
        if (source.length == 0 || target.length == 0) {
            return Pair.of(-1L, -1);
        }

        int m = source.length;
        int n = target.length;
        int cost = 0;
        int maxLen = 0;
        int location = -1;
        int[] p = new int[n];
        int[] d = new int[n];

        for (int i = 0; i < m; ++i) {
            for (int j = 0; j < n; ++j) {
                // calculate cost/score
                if (source[i] != target[j]) {
                    cost = 0;
                } else {
                    if ((i == 0) || (j == 0)) {
                        cost = 1;
                    } else {
                        cost = p[j - 1] + 1;
                    }
                }
                d[j] = cost;

                if (cost > maxLen) {
                    location = j;
                    maxLen = cost;
                }
            } // for {}

            int[] swap = p;
            p = d;
            d = swap;
        }

        return Pair.of((long)location, maxLen);
    }
}
