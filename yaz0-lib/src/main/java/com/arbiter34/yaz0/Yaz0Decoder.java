package com.arbiter34.yaz0;

import com.arbiter34.file.io.BinaryAccessFile;

import java.io.IOException;
import java.util.UUID;

public class Yaz0Decoder {
    public static final long MAGIC_BYTES = 0x59617A30;

    public static BinaryAccessFile decode(final BinaryAccessFile in) throws IOException {
        final BinaryAccessFile out = new BinaryAccessFile(UUID.randomUUID() + "-Decompressed", "rw");

        final long magicBytes = in.readUnsignedInt();
        if (magicBytes != MAGIC_BYTES) {
            throw new IOException(String.format("Invalid magic bytes found. Expected: %s Found %s", MAGIC_BYTES, magicBytes));
        }
        final long size = in.readUnsignedInt();
        final byte[] reserved = new byte[8];

        in.read(reserved);

        byte[] header = null;
        int headerPos = 0;
        while (in.getFilePointer() < in.length() && out.getFilePointer() < size) {
            if (header == null || headerPos == 8) {
                header = new byte[1];
                in.read(header);
                headerPos = 0;
            }
            if ((header[0] & (0x80 >>> headerPos)) > 0) {
                out.write(in.read());
            } else {
                int bytes = in.readUnsignedShort();
                long backReference = out.getFilePointer() - (bytes & 0x0000000000000FFF) - 1;

                int amountToCopy = (bytes >>> 12);
                if (amountToCopy == 0) {
                    amountToCopy = in.read() + 0x12;
                } else {
                    amountToCopy += 2;
                }

                while (amountToCopy-- > 0) {
                    long currentPosition = out.getFilePointer();
                    out.seek(backReference);
                    backReference += 1;
                    int b = out.read();
                    out.seek(currentPosition);
                    out.write(b);
                }
            }
            headerPos++;
        }
        out.seek(0);
        return out;
    }
}


//const u8 * src      = // pointer to start of source
//const u8 * src_end  = // pointer to end of source (last byte +1)
//u8 * dest           = // pointer to start of destination
//u8 * dest_end       = // pointer to end of destination (last byte +1)
//
//u8  group_head      = 0; // group header byte ...
//int group_head_len  = 0; // ... and it's length to manage groups
//
//while ( src < src_end && dest < dest_end )
//{
//if (!group_head_len)
//{
////*** start a new data group and read the group header byte.
//
//group_head = *src++;
//group_head_len = 8;
//}
//
//group_head_len--;
//if ( group_head & 0x80 )
//{
////*** bit in group header byte is set -> copy 1 byte direct
//
//*dest++ = *src++;
//}
//else
//{
////*** bit in group header byte is not set -> run length encoding
//
//// read the first 2 bytes of the chunk
//const u8 b1 = *src++;
//const u8 b2 = *src++;
//
//// calculate the source position
//const u8 * copy_src = dest - (( b1 & 0x0f ) << 8 | b2 ) - 1;
//
//// calculate the number of bytes to copy.
//int n = b1 >> 4;
//
//if (!n)
//n = *src++ + 0x12; // N==0 -> read third byte
//else
//n += 2; // add 2 to length
//ASSERT( n >= 3 && n <= 0x111 );
//
//// a validity check
//if ( copy_src < szs->data || dest + n > dest_end )
//return ERROR("Corrupted data!\n");
//
//// copy chunk data.
//// don't use memcpy() or memmove() here because
//// they don't work with self referencing chunks.
//while ( n-- > 0 )
//*dest++ = *copy_src++;
//}
//
//// shift group header byte
//group_head <<= 1;
//}
//
//// some assertions to find errors in debugging mode
//ASSERT( src <= src_end );
//ASSERT( dest <= dest_end );