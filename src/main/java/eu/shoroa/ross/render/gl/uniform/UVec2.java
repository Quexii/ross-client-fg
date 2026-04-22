package eu.shoroa.ross.render.gl.uniform;

public class UVec2 extends Uniform {
    private final float x;
    private final float y;
    public UVec2(String name, float x, float y) {
        super(name);
        this.x = x;
        this.y = y;
    }

    public float x() {
        return x;
    }

    public float y() {
        return y;
    }
}
