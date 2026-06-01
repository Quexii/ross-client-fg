package eu.shoroa.ross.module.impl.render;

import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.settings.NumberSetting;

public class ModuleAntiInvis extends Module {

    private final NumberSetting opacity = register(new NumberSetting("Opacity", "opacity", 1f, 0f, 1f, 0.1f));

    public ModuleAntiInvis() {
        super("AntiInvis", "Makes invisible players visible again.", Category.RENDER, null);
    }

    public float getOpacity() {
        return opacity.get();
    }
}
