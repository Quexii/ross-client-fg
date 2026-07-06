package eu.shoroa.ross.feature.setting;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, String id, boolean defaultValue) {
        super(name, id, defaultValue, Type.BOOLEAN);
    }

    public void toggle() {
        set(!get());
    }

    @Override
    public boolean setFromString(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            set(Boolean.parseBoolean(value));
            return true;
        }
        return false;
    }
}