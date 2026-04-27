package eu.shoroa.ross.gui.clickgui;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.gui.RossScreen;
import eu.shoroa.ross.gui.clickgui.elements.EleCategory;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;

public class ScreenClickGUI extends RossScreen {
    private final EleCategory[] categories = new EleCategory[Category.values().length];

    @Override
    protected void init() {
        for (int i = 0; i < categories.length; i++) {
            categories[i] = new EleCategory(Category.values()[i]);
        }

        float xi = 10f;

        for (EleCategory category : categories) {
            category.setX(xi);
            category.setY(10f);
            xi += category.getWidth() + 20f;
        }
    }

    @Override
    protected void render(float mouseX, float mouseY, float partialTicks) {
        Client.INSTANCE.skia.beginFrame();
        Renderer.use(Client.INSTANCE.skia);
        for (EleCategory category : categories) {
            Client.INSTANCE.skia.getCanvas().drawRectShadowNoclip(Rect.makeXYWH(category.getX(), category.getY(), category.getWidth(), category.getModulesHeight()), 0f, 0f, 16f, 2f, 0xAA000000);
        }

        for (EleCategory category : categories) {
            category.render(mouseX, mouseY, partialTicks);
        }

        try (Paint p = new Paint()) {
            p.setColor(0xFFd4d4d4);
            VariableFont.DerivedFont font = Fonts.GoogleFlex
                    .weight(500)
                    .opticSize(14);
            Renderer.drawText("FPS: " + Minecraft.getDebugFPS(), Display.getWidth() - 10f, Display.getHeight() - 10f, font, 14f, Font.Align.BOTTOM_RIGHT, p);
        }
        Client.INSTANCE.skia.endFrame();
    }

    @Override
    protected void input(float mouseX, float mouseY, EventInput event) {
        for (EleCategory category : categories) {
            category.input(mouseX, mouseY, event);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
