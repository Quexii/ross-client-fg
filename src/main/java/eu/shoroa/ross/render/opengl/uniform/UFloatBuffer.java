package eu.shoroa.ross.render.opengl.uniform;

import java.nio.FloatBuffer;

public class UFloatBuffer extends Uniform {
    private final FloatBuffer buffer;
    public UFloatBuffer(String name, FloatBuffer buffer) {
        super(name);
        this.buffer = buffer;
    }

    public FloatBuffer buffer() {
        return buffer;
    }
}