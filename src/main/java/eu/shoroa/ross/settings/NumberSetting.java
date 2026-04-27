package eu.shoroa.ross.settings;

public class NumberSetting extends Setting<Float> {

    private final float min;
    private final float max;
    private final float increment;

    public NumberSetting(String name, String id, float defaultValue,
                         float min, float max, float increment) {
        super(name, id, defaultValue, Type.NUMBER);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    @Override
    public void set(Float value) {
        value = Math.max(min, Math.min(max, value));
        value = Math.round(value / increment) * increment;
        super.set(value);
    }

    public float getMin() { return min; }
    public float getMax() { return max; }
    public float getIncrement() { return increment; }
}