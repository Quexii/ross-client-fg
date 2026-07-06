package eu.shoroa.ross.feature.setting;

import java.util.ArrayList;
import java.util.List;

public class SettingCategory {
    public final String name;
    public final String description;
    public final String id;

    private final List<Setting<?>> settings = new ArrayList<>();

    public SettingCategory(String name, String description, String id) {
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public void addSetting(Setting<?> setting) {
        settings.add(setting);
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    public static final SettingCategory ROOT() {
        return new SettingCategory("__root__", "root setting category", "sc::root");
    }

    public Setting<?> getSettingById(String id) {
        for (Setting<?> setting : settings) {
            if (setting.getId().equals(id)) {
                return setting;
            }
        }
        return null;
    }
}
