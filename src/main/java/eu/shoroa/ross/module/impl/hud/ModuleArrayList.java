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
import eu.shoroa.ross.ui.api.*;
import eu.shoroa.ross.util.render.Renderer2D;
import io.github.humbleui.skija.ImageFilter;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.yoga.Yoga;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static eu.shoroa.ross.Client.mc;

public class ModuleArrayList extends Module {
    private final ModeSetting<Mode> mode = register(new ModeSetting<>("Mode", "mode", Mode.VANILLA));
    private final ModeSetting<Position> position = register(new ModeSetting<>("Position", "position", Position.TOP_RIGHT));

    private boolean isTop = false;
    private boolean isLeft = false;
    private String currentModeString = "";
    private boolean wasWatermark = false;
    private float currentWatermarkTop = 0f;
    private String modulesSignature = "";
    private final List<Module> cachedModules = new ArrayList<>();
    private boolean warnedWatermarkConfig = false;

    Node rootNode = new Node();
    Node modulesNode = new Node();

    public ModuleArrayList() {
        super("ArrayList", "Displays enabled modules on the screen.", Category.HUD);


        isLeft = position.get() == Position.TOP_LEFT || position.get() == Position.BOTTOM_LEFT;
        isTop = position.get() == Position.TOP_RIGHT || position.get() == Position.TOP_LEFT;

        rootNode.width(Display.getWidth());
        rootNode.height(Display.getHeight());
        rootNode.direction(Direction.COLUMN);
        rootNode.justify(isTop ? Justify.FLEX_START : Justify.FLEX_END);
        rootNode.alignItems(isLeft ? Align.FLEX_START : Align.FLEX_END);

        modulesNode.direction(isTop ? Direction.COLUMN : Direction.COLUMN_REVERSE);
        modulesNode.alignItems(isLeft ? Align.FLEX_START : Align.FLEX_END);
        modulesNode.margin(14f);

        rootNode.children(modulesNode);
    }

    @Subscribe
    public void oe$TopSkia(EventHUD.TopSkia event) {
        if (mode.get() != Mode.SKIA) return;

        Font font = Fonts.GoogleFlex
                .weight(400)
                .roundness(100)
                .opticSize(24);

        float textSize = 16f;
        List<Module> modules = cachedModules;
        int count = Math.min(modulesNode.children.size(), modules.size());
        for (int i = 0; i < count; i++) {
            Node node = (Node) modulesNode.children.get(i);
            Module module = modules.get(i);
            try (Paint p = new Paint()) {
                p.setColor(-1);
                p.setImageFilter(ImageFilter.makeDropShadow(0f, 0f, 2f, 2f, 0xFF000000));
                Renderer.drawText(module.name, node.getX(), node.getY(), font, textSize, Font.Align.TOP_LEFT, p);
                p.setImageFilter(null);
                Renderer.drawText(module.name, node.getX(), node.getY(), font, textSize, Font.Align.TOP_LEFT, p);
            }
        }
    }

    @Subscribe
    public void oe$TopVanilla(EventHUD.TopVanilla event) {
        // called before bottom skia so we update values here
        boolean newIsLeft = position.get() == Position.TOP_LEFT || position.get() == Position.BOTTOM_LEFT;
        boolean newIsTop = position.get() == Position.TOP_RIGHT || position.get() == Position.TOP_LEFT;
        boolean alignmentChanged = newIsLeft != isLeft || newIsTop != isTop;
        isLeft = newIsLeft;
        isTop = newIsTop;
        if (alignmentChanged) {
            rootNode.justify(isTop ? Justify.FLEX_START : Justify.FLEX_END);
            rootNode.alignItems(isLeft ? Align.FLEX_START : Align.FLEX_END);
            modulesNode.direction(isTop ? Direction.COLUMN : Direction.COLUMN_REVERSE);
            modulesNode.alignItems(isLeft ? Align.FLEX_START : Align.FLEX_END);
        }

        ModuleWatermark watermark = (ModuleWatermark) ModuleManager.getModule("Watermark");
        ModeSetting<?> watermarkStyle = watermark == null
                ? null
                : (ModeSetting<?>) watermark.getSettingById("style");

        Mode currentMode = mode.get();
        float topScale = currentMode == Mode.VANILLA ? 0.5f : 1f;
        boolean watermarkConfigValid = watermark != null && watermarkStyle != null && watermarkStyle.getCurrent() != null;
        if (!watermarkConfigValid && !warnedWatermarkConfig) {
            System.err.println("ArrayList: watermark module or style missing; skipping watermark offset.");
            warnedWatermarkConfig = true;
        }
        boolean watermarkEnabled = watermarkConfigValid && watermark.isEnabled();
        boolean watermarkActive = isTop && isLeft && watermarkEnabled;
        String watermarkStyleName = watermarkConfigValid ? watermarkStyle.getCurrent().displayName() : "";
        boolean watermarkChanged = false;
        boolean modulesChanged = false;

        float skiaTextSize = 16f;
        Font skiaFont = null;
        List<Module> modules = ModuleManager.getEnabledModules();

        if (currentMode == Mode.SKIA) {
            final Font currentSkiaFont = Fonts.GoogleFlex
                    .weight(400)
                    .roundness(100)
                    .opticSize(24);
            final float currentSkiaTextSize = skiaTextSize;
            skiaFont = currentSkiaFont;
            modules.sort(Comparator.comparingDouble(module -> Renderer.getTextBounds(((Module) module).name, currentSkiaFont, currentSkiaTextSize).width).reversed());
        } else {
            modules.sort((module1, module2) -> mc.fontRendererObj.getStringWidth(module2.name) - mc.fontRendererObj.getStringWidth(module1.name));
        }

        StringBuilder modulesSignatureBuilder = new StringBuilder(currentMode.name());
        for (Module module : modules) {
            modulesSignatureBuilder.append('|').append(module.name);
        }
        String nextModulesSignature = modulesSignatureBuilder.toString();

        if (!nextModulesSignature.equals(modulesSignature)) {
            modulesSignature = nextModulesSignature;
            cachedModules.clear();
            cachedModules.addAll(modules);
            modulesNode.clearChildren();
            for (Module module : modules) {
                Node moduleNode = new Node();
                if (currentMode == Mode.SKIA) {
                    Size size = Renderer.getTextBounds(module.name, skiaFont, skiaTextSize);
                    moduleNode.width(size.width);
                    moduleNode.height(size.height);
                } else {
                    moduleNode.width(mc.fontRendererObj.getStringWidth(module.name) * 2f);
                    moduleNode.height(20f);
                }
                modulesNode.children(moduleNode);
            }
            modulesChanged = true;
        }

        if (watermarkActive) {
            if (!currentModeString.equalsIgnoreCase(watermarkStyleName)) {
                currentModeString = watermarkStyleName;
            }

            float desiredTop = currentWatermarkTop;
            if (watermarkStyleName.equalsIgnoreCase("ross")) desiredTop = 16f;
            else if (watermarkStyleName.equalsIgnoreCase("weedsense")) desiredTop = 32f;
            else if (watermarkStyleName.equalsIgnoreCase("weedhack")) desiredTop = 120f;

            if (desiredTop != currentWatermarkTop) {
                modulesNode.top(desiredTop);
                currentWatermarkTop = desiredTop;
                watermarkChanged = true;
            }
        } else {
            if (currentWatermarkTop != 0f) {
                modulesNode.top(0f);
                currentWatermarkTop = 0f;
                watermarkChanged = true;
            }
            if (!currentModeString.isEmpty()) {
                currentModeString = "";
            }
        }

        if (wasWatermark != watermarkEnabled) {
            wasWatermark = watermarkEnabled;
            watermarkChanged = true;
        }

        if (watermarkChanged || modulesChanged || alignmentChanged) {
            rootNode.markDirty();
        }

        if (rootNode.getWidth() != Display.getWidth() || rootNode.getHeight() != Display.getHeight()) {
            rootNode.width(Display.getWidth());
            rootNode.height(Display.getHeight());
            rootNode.markDirty();
        }

        if (rootNode.consumeDirty()) {
            rootNode.calcLayout(Yoga.YGUndefined, Yoga.YGUndefined, LayoutDirection.LTR);
            rootNode.resolveAbsolutePositions(0, 0);
        }

        if (currentMode != Mode.VANILLA) return;
        List<Module> renderModules = cachedModules;

        Renderer2D.begin2d();
        int count = Math.min(modulesNode.children.size(), renderModules.size());
        for (int i = 0; i < count; i++) {
            Node node = (Node) modulesNode.children.get(i);
            Module module = renderModules.get(i);
            GlStateManager.pushMatrix();
            GlStateManager.scale(2f, 2f, 1f);
            Renderer2D.drawString(module.name, node.getX() / 2f, node.getY() / 2f, -1, true);
            GlStateManager.popMatrix();
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
