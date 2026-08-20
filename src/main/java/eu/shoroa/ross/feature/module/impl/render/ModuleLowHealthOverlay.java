package eu.shoroa.ross.feature.module.impl.render;

import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.event.api.EventPriority;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.animate.Easing;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.ui.UI;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Shader;
import org.lwjgl.opengl.Display;

import static eu.shoroa.ross.Client.mc;

public class ModuleLowHealthOverlay extends Module {
    private final SettingCategory settings = addCategory("Settings", "settings", "settings");
    private final NumberSetting healthThreshold = register(new NumberSetting("Health Threshold %", "health_threshold", 20, 1, 100, 1), settings);

    private final Animate animate = new Animate(120L, Easing.LINEAR);

    public ModuleLowHealthOverlay() {
        super("Low Health Overlay", "Shows a red overlay when health is low.", Category.HUD, MaterialIcons.MONITOR_HEART);
    }

    @Subscribe(priority = EventPriority.HIGHEST)
    public void onHud(Hud.Layer event) {
        if (mc.thePlayer == null) return;
        if (event.is(Hud.Layer.NAME_SKIA_BOTTOM)) {

            final float w = Display.getWidth();
            final float h = Display.getHeight();

            animate.doEase(mc.thePlayer.getHealth() <= (mc.thePlayer.getMaxHealth() / 100f * healthThreshold.get()));

            int color = Color.makeLerp(0x00FF0000, 0xAAFF0000, (float) animate.getValue());

            float radius = Math.max(w, h) * 0.7f;

            float minRatio = Math.min(w, h);

            float growAspectX = w / minRatio;
            float growAspectY = h / minRatio;

            UI.save();
            UI.scale(growAspectX, growAspectY);
            try (Paint p = new Paint()) {
                p.setShader(Shader.makeRadialGradient(minRatio / 2f, minRatio / 2f, radius, new int[]{0x00FF0000, color}));
                UI.drawRect(0f, 0f, minRatio, minRatio, p);
            }
            UI.restore();
        }
    }
}