package com.arbiter34.byml.nodes;

import com.arbiter34.file.io.BinaryAccessFile;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PathTableNode {
    public static final short NODE_TYPE = 0xC3;

    @JsonCreator
    public static PathTableNode parse(@JsonProperty("offset") long offset, @JsonProperty("file") BinaryAccessFile file) {
        throw new UnsupportedOperationException();
    }

}
