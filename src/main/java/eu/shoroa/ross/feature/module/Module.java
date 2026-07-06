package eu.shoroa.ross.feature.module;

import eu.shoroa.ross.feature.setting.Setting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static eu.shoroa.ross.Client.EVENT_BUS;

public class Module {
    public final String name;
    public final String description;
    public final Category category;
    public final String icon;

    public Bind bind;

    private boolean enabled = false;

    private final List<SettingCategory> settings = new ArrayList<>();

    public Module(String name, String description, Category category, Bind bind, String icon) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.bind = bind;
        this.icon = icon;
    }

    public Module(String name, String description, Category category, String icon) {
        this(name, description, category, null, icon);
    }

    public void toggle() {
        setEnabled(!isEnabled());
    }

    public void onEnable() {
        EVENT_BUS.register(this);
    }

    public void onDisable() {
        EVENT_BUS.unregister(this);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (enabled) onEnable();
        else onDisable();
    }

    public <T extends Setting<?>> T register(T setting, SettingCategory category) {
        category.addSetting(setting);
        return setting;
    }

    public SettingCategory addCategory(String name, String description, String id) {
        SettingCategory category = new SettingCategory(name, description, id);
        settings.add(category);
        return category;
    }

    public SettingCategory addCategory(SettingCategory category) {
        settings.add(category);
        return category;
    }

    public List<SettingCategory> getSettings() {
        return settings;
    }

    public Setting<?> getSettingById(String id) {
        for (SettingCategory category : settings) {
            Setting<?> setting = category.getSettingById(id);
            if (setting != null) {
                return setting;
            }
        }
        return null;
    }
}