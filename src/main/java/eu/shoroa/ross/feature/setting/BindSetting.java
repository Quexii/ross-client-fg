package eu.shoroa.ross.feature.setting;

import eu.shoroa.ross.feature.module.Bind;

/** Holds a module keybind; the value is null when unbound. */
public class BindSetting extends Setting<Bind> {

    public BindSetting(String name, String id, Bind defaultBind) {
        super(name, id, defaultBind, Type.BIND);
    }

    @Override
    public boolean setFromString(String value) {
        if (value.equalsIgnoreCase("none")) {
            set(null);
            return true;
        }

        Bind bind = Bind.fromConfigString(value);
        if (bind == null) return false;
        set(bind);
        return true;
    }

    public String toConfigString() {
        return get() == null ? "none" : get().toConfigString();
    }
}
