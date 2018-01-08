package com.arbiter34.prod;

import com.arbiter34.file.io.BinaryAccessFile;
import com.arbiter34.file.util.FloatUtil;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

public class MeshInstance {
    
    private final Point translate;
    private final Point rotation;
    private final float scale;

    @JsonCreator
    public MeshInstance(@JsonProperty("translate") Point translate, @JsonProperty("rotation") Point rotation,
                        @JsonProperty("scale") float scale) {
        this.translate = translate;
        this.rotation = rotation;
        this.scale = scale;
    }

    public static MeshInstance parse(final BinaryAccessFile file) throws IOException {
        final float translateX = FloatUtil.parseNullSafe(file.readUnsignedInt());
        final float translateY = FloatUtil.parseNullSafe(file.readUnsignedInt());
        final float translateZ = FloatUtil.parseNullSafe(file.readUnsignedInt());
        final Point translate = new Point(translateX, translateY, translateZ);
        
        final float rotationX = FloatUtil.parseNullSafe(file.readUnsignedInt());
        final float rotationY = FloatUtil.parseNullSafe(file.readUnsignedInt());
        final float rotationZ = FloatUtil.parseNullSafe(file.readUnsignedInt());
        final Point rotation = new Point(rotationX, rotationY, rotationZ);

        final float scale = FloatUtil.parseNullSafe(file.readUnsignedInt());
        // padding
        file.read(new byte[4]);
        
        return new MeshInstance(translate, rotation, scale);
    }
    
    public void write(final BinaryAccessFile file) throws IOException {
        final int translateXBits = Float.floatToIntBits(translate.getX());
        final int translateYBits = Float.floatToIntBits(translate.getY());
        final int translateZBits = Float.floatToIntBits(translate.getZ());
        
        final int rotationXBits = Float.floatToIntBits(rotation.getX());
        final int rotationYBits = Float.floatToIntBits(rotation.getY());
        final int rotationZBits = Float.floatToIntBits(rotation.getZ());
        
        final int scaleBits = Float.floatToIntBits(scale);
        
        file.writeUnsignedInt(translateXBits);
        file.writeUnsignedInt(translateYBits);
        file.writeUnsignedInt(translateZBits);

        file.writeUnsignedInt(rotationXBits);
        file.writeUnsignedInt(rotationYBits);
        file.writeUnsignedInt(rotationZBits);

        file.writeUnsignedInt(scaleBits);

        file.write(new byte[4]);
    }

    public Point getTranslate() {
        return translate;
    }

    public Point getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }
}
