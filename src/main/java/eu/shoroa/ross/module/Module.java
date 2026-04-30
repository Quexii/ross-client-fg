package eu.shoroa.ross.module;

import eu.shoroa.ross.settings.Setting;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;

public class Module {
    public final String name;
    public final String description;
    public final Category category;
    public Bind bind;

    private boolean enabled = false;

    public List<Setting<?>> getSettings() {
        return settings;
    }

    private final List<Setting<?>> settings = new ArrayList<>();

    public Module(String name, String description, Category category, Bind bind) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.bind = bind;
    }

    public Module(String name, String description, Category category) {
        this(name, description, category, null);
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

    public <T extends Setting<?>> T register(T setting) {
        settings.add(setting);
        return setting;
    }
}
