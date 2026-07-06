package eu.shoroa.ross.render.opengl.uniform;

public class UInt extends Uniform {
    private final int value;

    public UInt(String name, int value) {
        super(name);
        this.value = value;
    }

    public int value() {
        return value;
    }
}