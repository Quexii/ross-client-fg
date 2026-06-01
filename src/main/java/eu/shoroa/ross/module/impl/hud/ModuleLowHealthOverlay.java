package eu.shoroa.ross.module.impl.hud;

import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.EventPriority;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.settings.NumberSetting;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Shader;
import org.lwjgl.opengl.Display;

import static eu.shoroa.ross.Client.mc;

public class ModuleLowHealthOverlay extends Module {
    private final Animate animate = new Animate(120L, Easing.LINEAR);

    private final NumberSetting healthThreshold = register(new NumberSetting("Health Threshold %", "health_threshold", 20, 1, 100, 1));

    public ModuleLowHealthOverlay() {
        super("Low Health Overlay", "Shows a red overlay when health is low.", Category.HUD);
    }

    // Highest priority to render the overlay before other HUD elements
    @Subscribe(priority = EventPriority.HIGHEST)
    public void oe$BottomSkia(EventHUD.BottomSkia event) {
        if (mc.thePlayer == null) return;

        final float w = Display.getWidth();
        final float h = Display.getHeight();

        animate.doEase(mc.thePlayer.getHealth() <= (mc.thePlayer.getMaxHealth() / 100f * healthThreshold.get()));

        int color = Color.makeLerp(0x00FF0000, 0xAAFF0000, (float) animate.getValue());

        float radius = Math.max(w, h) * 0.7f;

        float minRatio = Math.min(w, h);

        float growAspectX = w / minRatio;
        float growAspectY = h / minRatio;

        Renderer.save();
        Renderer.scale(growAspectX, growAspectY);
        try (Paint p = new Paint()) {
            p.setShader(Shader.makeRadialGradient(minRatio / 2f, minRatio / 2f, radius, new int[]{0x00FF0000, color}));
            Renderer.drawRect(0f, 0f, minRatio, minRatio, p);
        }
        Renderer.restore();
    }
}
