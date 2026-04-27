package eu.shoroa.ross.gui.clickgui.elements;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.gui.GuiElement;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.RRect;

import java.util.Arrays;

import static eu.shoroa.ross.Client.mc;

public class EleCategory extends GuiElement {
    private final Category category;

    private final Animate hoverAnimate = new Animate(100, Easing.CUBIC_IN_OUT);

    private final EleModule[] modules;

    public EleCategory(Category category) {
        super(0f, 0f, 220f, 40f);
        this.category = category;

        Module[] catModules = ModuleManager.getModulesByCategory(category);

        modules = new EleModule[catModules.length];

        for (int i = 0; i < modules.length; i++) {
            modules[i] = new EleModule(catModules[i]);
        }
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        final float cornerRadius = 10f;

        if (getBounds().contains(mouseX, mouseY)) hoverAnimate.forceFinish();

        hoverAnimate.doEase(getBounds().contains(mouseX, mouseY));

        float totalHeight = getHeight();
        float[] moduleHeights = new float[modules.length];
        for (int i = 0; i < modules.length; i++) {
            moduleHeights[i] = modules[i].getExtendHeight();
            totalHeight += moduleHeights[i];
        }

        try (Paint p = new Paint()) {
            p.setColor(0xFF202020);
            Renderer.drawRRect(getX(), getY(), getWidth(), totalHeight, cornerRadius, p);
        }

        try (Paint p = new Paint()) {
            p.setColor(0xffd4d4d4);
            VariableFont.DerivedFont font = Fonts.GoogleFlex
                    .weight(600)
                    .roundness(100)
                    .opticSize(24);
            Renderer.drawText(category.name(), getX() + getWidth() / 2f, getY() + getHeight() / 2f, font, 18f, Font.Align.CENTER, p);
        }

        try (Paint p = new Paint()) {
            p.setStroke(true);
            p.setStrokeWidth(2f);
            p.setColor(0xFF1b1b1b);
            Renderer.drawRRect(getX(), getY(), getWidth(), totalHeight, cornerRadius, p);
        }

        Client.INSTANCE.skia.getCanvas().save();
        Client.INSTANCE.skia.getCanvas().clipRRect(RRect.makeXYWH(getX(), getY() + getHeight(), getWidth(), totalHeight - getHeight(), cornerRadius), true);
        float my = getHeight();
        for (int i = 0; i < modules.length; i++) {
            EleModule module = modules[i];
            module.render(mouseX, mouseY, partialTicks);
            module.setX(getX());
            module.setY(getY() + my);
            my += moduleHeights[i];
        }
        Client.INSTANCE.skia.getCanvas().restore();
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        for (EleModule module : modules) {
            if (module.input(mouseX, mouseY, event)) {
                return true;
            }
        }
        return false;
    }

    public float getModulesHeight() {
        return Arrays.stream(modules).map(EleModule::getExtendHeight).reduce(getHeight(), Float::sum);
    }
}
