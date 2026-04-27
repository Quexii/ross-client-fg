package eu.shoroa.ross.settings;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, String id, boolean defaultValue) {
        super(name, id, defaultValue, Type.BOOLEAN);
    }

    public void toggle() {
        set(!get());
    }
}