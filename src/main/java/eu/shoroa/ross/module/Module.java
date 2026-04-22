package eu.shoroa.ross.module;

import net.minecraftforge.common.MinecraftForge;

public class Module {
    public final String name;
    public final String description;
    public final Category category;
    public final Bind bind;

    private boolean enabled = false;

    public Module(String name, String description, Category category, Bind bind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.bind = bind;
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }
}
