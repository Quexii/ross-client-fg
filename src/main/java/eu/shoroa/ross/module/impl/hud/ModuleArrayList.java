package eu.shoroa.ross.module.impl.hud;

import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.settings.ModeEnum;
import eu.shoroa.ross.settings.ModeSetting;
import eu.shoroa.ross.types.Size;
import eu.shoroa.ross.util.render.Renderer2D;
import io.github.humbleui.skija.ImageFilter;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.Display;

import java.util.Comparator;
import java.util.List;

import static eu.shoroa.ross.Client.mc;

public class ModuleArrayList extends Module {

    private final ModeSetting<Mode> mode = register(new ModeSetting<>("Mode", "arraylist.mode", Mode.VANILLA));
    private final ModeSetting<Position> position = register(new ModeSetting<>("Position", "arraylist.position", Position.TOP_RIGHT));

    public ModuleArrayList() {
        super("ArrayList", "Displays enabled modules on the screen", Category.HUD);
    }

    @Subscribe
    public void oe$SkiaTop(EventHUD.TopSkia event) {
        if (mode.get() != Mode.SKIA) return;

        Font font = Fonts.GoogleFlex.weight(500);
        float textSize = 18f;
        List<Module> modules = ModuleManager.getEnabledModules();

        modules.sort(Comparator.comparingDouble(module -> Renderer.getTextBounds(((Module) module).name, font, textSize).width).reversed());

        boolean isLeft = position.get() == Position.TOP_LEFT || position.get() == Position.BOTTOM_LEFT;
        boolean isTop = position.get() == Position.TOP_RIGHT || position.get() == Position.TOP_LEFT;

        float y = 8f;

        ModuleWatermark watermark = (ModuleWatermark) ModuleManager.getModule("Watermark");
        ModeSetting<?> watermarkStyle = (ModeSetting<?>) watermark.getSettingById("watermark.style");

        if(isTop && isLeft && watermark.isEnabled()) {
            if (watermarkStyle.getCurrent().displayName().equalsIgnoreCase("ross")) y += 20f;
            else if (watermarkStyle.getCurrent().displayName().equalsIgnoreCase("weedsense")) y += 32f;
            else if (watermarkStyle.getCurrent().displayName().equalsIgnoreCase("weedhack")) y += 120f;
        }

        for (Module module : modules) {
            Size size = Renderer.getTextBounds(module.name, font, textSize);

            float xPos = !isLeft ? Display.getWidth() - size.width - 16 : 16;
            float yPos = isTop ? y : Display.getHeight() - y;

            try (Paint p = new Paint()) {
                p.setColor(-1);
                p.setImageFilter(ImageFilter.makeDropShadow(0f, 0f, 2f, 2f, 0xFF000000));
                Renderer.drawText(module.name, xPos, yPos, font, textSize, Font.Align.TOP_LEFT, p);
                p.setImageFilter(null);
                Renderer.drawText(module.name, xPos, yPos, font, textSize, Font.Align.TOP_LEFT, p);
            }
            y += size.height;
        }
    }

    @Subscribe
    public void oe$VanillaTop(EventHUD.TopVanilla event) {
        if (mode.get() != Mode.VANILLA) return;

        List<Module> modules = ModuleManager.getEnabledModules();

        modules.sort((module1, module2) -> mc.fontRendererObj.getStringWidth(module2.name) - mc.fontRendererObj.getStringWidth(module1.name));

        boolean isLeft = position.get() == Position.TOP_LEFT || position.get() == Position.BOTTOM_LEFT;
        boolean isTop = position.get() == Position.TOP_RIGHT || position.get() == Position.TOP_LEFT;

        float y = 8f;

        ModuleWatermark watermark = (ModuleWatermark) ModuleManager.getModule("Watermark");
        ModeSetting<?> watermarkStyle = (ModeSetting<?>) watermark.getSettingById("watermark.style");

        if(isTop && isLeft && watermark.isEnabled()) {
            if (watermarkStyle.getCurrent().displayName().equalsIgnoreCase("ross")) y += 8f;
            else if (watermarkStyle.getCurrent().displayName().equalsIgnoreCase("weedsense")) y += 16f;
            else if (watermarkStyle.getCurrent().displayName().equalsIgnoreCase("weedhack")) y += 60f;
        }

        Renderer2D.begin2d();
        GlStateManager.scale(2f, 2f, 1f);
        for (Module module : modules) {
            float xPos = !isLeft ? Display.getWidth() / 2f - mc.fontRendererObj.getStringWidth(module.name) - 8 : 8;
            float yPos = isTop ? y : Display.getHeight() / 2f - y;

            Renderer2D.drawString(module.name, xPos, yPos, -1, true);
            y += 10f;
        }
        Renderer2D.end2d();
    }

    private enum Mode implements ModeEnum {
        VANILLA("Vanilla"),
        SKIA("Ross");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String displayName() {
            return name;
        }
    }

    private enum Position implements ModeEnum {
        TOP_LEFT("Top Left"),
        TOP_RIGHT("Top Right"),
        BOTTOM_LEFT("Bottom Left"),
        BOTTOM_RIGHT("Bottom Right");

        private final String name;

        Position(String name) {
            this.name = name;
        }

        @Override
        public String displayName() {
            return name;
        }
    }
}
