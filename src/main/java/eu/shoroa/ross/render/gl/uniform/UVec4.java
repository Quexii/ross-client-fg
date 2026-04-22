package eu.shoroa.ross.render.gl.uniform;

public class UVec4 extends Uniform {
    private final float x;
    private final float y;
    private final float z;
    private final float w;
    public UVec4(String name, float x, float y, float z, float w) {
        super(name);
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }

    public float z() {
        return z;
    }

    public float w() {
        return w;
    }
}
