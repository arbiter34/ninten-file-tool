package com.arbiter34.prod;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Point {

    private final float x;
    private final float y;
    private final float z;

    @JsonCreator
    public Point(@JsonProperty("x") float x, @JsonProperty("y") float y, @JsonProperty("z") float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }
}
