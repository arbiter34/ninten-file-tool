package com.arbiter34.byml.io;

import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BinaryAccessFile  extends RandomAccessFile {

    public BinaryAccessFile(String name, String mode) throws FileNotFoundException {
        super(name, mode);
    }

    public BinaryAccessFile(File file, String mode) throws FileNotFoundException {
        super(file, mode);
    }

    public void writeUnsignedInt(long v) throws IOException {
        int u = (int)v;
        write((u >>> 24) & 0xFF);
        write((u >>> 16) & 0xFF);
        write((u >>>  8) & 0xFF);
        write((u) & 0xFF);
    }

    public long readUnsignedInt() throws IOException {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }
}
