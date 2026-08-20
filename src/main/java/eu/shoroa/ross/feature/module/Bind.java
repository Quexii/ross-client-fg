package eu.shoroa.ross.feature.module;

import eu.shoroa.ross.event.EventInput;
import org.lwjgl.input.Keyboard;

import java.util.Objects;

public class Bind {
    public final int key;
    public final EventInput.Type type;
    public final EventInput.Action action;

    public Bind(int key, EventInput.Type type, EventInput.Action action) {
        this.key = key;
        this.type = type;
        this.action = action;
    }

    public String displayName() {
        if (type == EventInput.Type.MOUSE) {
            return "M" + (key + 1);
        }
        String name = Keyboard.getKeyName(key);
        return name != null ? name : "Key " + key;
    }

    public String toConfigString() {
        return type.name() + ":" + key + ":" + action.name();
    }

    public static Bind fromConfigString(String value) {
        String[] parts = value.split(":");
        if (parts.length != 3) return null;
        try {
            return new Bind(
                    Integer.parseInt(parts[1]),
                    EventInput.Type.valueOf(parts[0]),
                    EventInput.Action.valueOf(parts[2]));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bind)) return false;
        Bind bind = (Bind) o;
        return key == bind.key && type == bind.type && action == bind.action;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, type, action);
    }
}
