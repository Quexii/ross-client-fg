package eu.shoroa.ross.module.impl.hud;

import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.render.skia.image.ImageSource;
import eu.shoroa.ross.render.skia.image.Images;
import eu.shoroa.ross.settings.ModeEnum;
import eu.shoroa.ross.settings.ModeSetting;
import eu.shoroa.ross.util.render.Renderer2D;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.ImageFilter;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.renderer.GlStateManager;

@SuppressWarnings("ALL")
public class ModuleWatermark extends Module {

    private final ModeSetting<Mode> mode = register(new ModeSetting<>("Mode", "watermark.style", Mode.ROSS));

    public ModuleWatermark() {
        super("Watermark", "Displays a watermark on the screen", Category.HUD, null);
    }

    @Subscribe
    public void oe$BottomVanilla(EventHUD.BottomVanilla event) {
        if (mode.get() == Mode.WEEDSENSE) {
            Renderer2D.begin2d();
            GlStateManager.scale(2f, 2f, 1.0f);

            float totalWidth = 122f;

            Renderer2D.drawRect(2f, 2f, totalWidth, 16f, 0xFF000000);
            Renderer2D.drawRect(2.5f, 2.5f, totalWidth-1, 15f, 0xFF383838);
            Renderer2D.drawRect(3f, 3f, totalWidth-2, 14f, 0xFF222222);
            Renderer2D.drawRect(3.5f, 3.5f, totalWidth-3, 13f, 0xFF383838);
            Renderer2D.drawRect(4f, 4f, totalWidth-4, 12f, 0xFF000000);
            for (int i = 0; i < (totalWidth-6); i++) {
                long interval = 2000;
                long timer = System.currentTimeMillis() % interval;
                float step = (360f / (totalWidth-6)) / 3f;
                float hue = (float) timer / interval * 360f - i * step;
                if (hue < 0) hue += 360f;

                int color = Color.makeFromHSV(new float[] {hue, 0.7f, 1f});
                Renderer2D.drawRect(5f + i, 5f, 1f, 1f, color);
            }
            GlStateManager.translate(0f, 0.5f, 0f);
            Renderer2D.drawString("Weedhack Premium Beta", 6f, 7f, 0xFFFFFFFF, false);
            Renderer2D.end2d();
        }
    }

    @Subscribe
    public void oe$BottomSkia(EventHUD.BottomSkia event) {
        if (mode.get() == Mode.WEEDHACK) {
            ImageSource img = Images.WATERMARK_WEEDHACK;
            Renderer.drawImage(img, 4f, 4f, img.getWidth() / 4f, img.getHeight() / 4f);
        }

        if (mode.get() == Mode.ROSS) {
            try (Paint p = new Paint()) {
                p.setColor(0xFFe044a8);
                p.setImageFilter(ImageFilter.makeDropShadow(0f, 0f, 5f, 5f, 0x88e044a8));
                VariableFont.DerivedFont font = Fonts.GoogleFlex.weight(600).opticSize(24).roundness(100);
                Renderer.drawText("Ross", 4f, 0f, font, 22f, Font.Align.TOP_LEFT, p);
            }
        }
    }

    @Subscribe
    public void oe$TopVanilla(EventHUD.TopVanilla event) {

    }

    @Subscribe
    public void oe$TopSkia(EventHUD.TopSkia event) {

    }

    private enum Mode implements ModeEnum {
        ROSS("Ross"), WEEDSENSE("Weedsense"), WEEDHACK("Weedhack");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String displayName() {
            return name;
        }
    }
}
