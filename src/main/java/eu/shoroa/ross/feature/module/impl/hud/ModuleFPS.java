package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.feature.module.HUDAnchor;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import net.minecraft.client.Minecraft;

public class ModuleFPS extends TextHUDModule {
    public ModuleFPS() {
        super("FPS", "Shows the current frame rate", MaterialIcons.SPEED, MaterialIcons.SPEED);
        setDefaultPosition(HUDAnchor.LEFT_TOP, 10, 64);
    }

    @Override
    protected String value() {
        return Integer.toString(Minecraft.getDebugFPS());
    }

    @Override
    protected String suffix() {
        return "FPS";
    }
}
