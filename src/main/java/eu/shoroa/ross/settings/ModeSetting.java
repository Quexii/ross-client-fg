package eu.shoroa.ross.settings;

import java.util.Arrays;
import java.util.List;

public class ModeSetting<T extends Enum<T> & ModeEnum> extends Setting<T> {
    private final T[] modes;
    private int currentIndex;

    public ModeSetting(String name, T defaultValue) {
        super(name, defaultValue, Type.MODE);
        this.modes = defaultValue.getDeclaringClass().getEnumConstants();
        this.currentIndex = Arrays.asList(modes).indexOf(defaultValue);
    }

    public List<T> getModes() {
        return Arrays.asList(modes);
    }

    public T getCurrent() {
        return modes[currentIndex];
    }

    @Override
    public void set(T newValue) {
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(newValue)) {
                currentIndex = i;
                break;
            }
        }
        super.set(newValue);
    }

    public void nextMode() {
        currentIndex = (currentIndex + 1) % modes.length;
        super.set(modes[currentIndex]);
    }

    public void prevMode() {
        currentIndex = (currentIndex - 1 + modes.length) % modes.length;
        super.set(modes[currentIndex]);
    }

    public String getCurrentString() {
        return modes[currentIndex].name();
    }

    public String getCurrentDisplayName() {
        return modes[currentIndex].displayName();
    }

    public void setByName(String name) {
        for (T m : modes) {
            if (m.name().equalsIgnoreCase(name)) {
                set(m);
                return;
            }
        }
    }

    public void setByDisplayName(String display) {
        for (T m : modes) {
            if (m.displayName().equalsIgnoreCase(display)) {
                set(m);
                return;
            }
        }
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setIndex(int index) {
        if (index < 0 || index >= modes.length) return;
        currentIndex = index;
        super.set(modes[currentIndex]);
    }
}