package eu.shoroa.ross.render.opengl.uniform;

public class UFloat extends Uniform {
    private final float value;
    public UFloat(String name, float value) {
        super(name);
        this.value = value;
    }

    public float value() {
        return value;
    }
}