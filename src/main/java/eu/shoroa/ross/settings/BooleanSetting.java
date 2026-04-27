package eu.shoroa.ross.settings;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue, Type.BOOLEAN);
    }

    public void toggle() {
        set(!get());
    }
}