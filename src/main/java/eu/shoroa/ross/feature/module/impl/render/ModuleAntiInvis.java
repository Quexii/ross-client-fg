package eu.shoroa.ross.feature.module.impl.render;

import eu.shoroa.ross.feature.module.Bind;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.skia.font.MaterialIcons;

public class ModuleAntiInvis extends Module {
    private final SettingCategory categorySettings = addCategory("Settings", ".", "settings");
    private final NumberSetting opacity = register(new NumberSetting("Opacity", "opacity", 40, 1, 255, 1), categorySettings);

    public ModuleAntiInvis() {
        super("Anti Invis", ".", Category.RENDER, MaterialIcons.PERSON_SEARCH);
    }

    public float getOpacity() {
        return opacity.get().floatValue() / 255f;
    }
}
