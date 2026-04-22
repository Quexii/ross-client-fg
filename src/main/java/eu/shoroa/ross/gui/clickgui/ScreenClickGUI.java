package eu.shoroa.ross.gui.clickgui;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.gui.RossScreen;
import eu.shoroa.ross.gui.clickgui.elements.EleCategory;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.render.Renderer;

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
            xi += category.getWidth() + 10f;
        }
    }

    @Override
    protected void render(float mouseX, float mouseY, float partialTicks) {
        Client.INSTANCE.skia.beginFrame();
        Renderer.use(Client.INSTANCE.skia);
        for (EleCategory category : categories) {
            category.render(mouseX, mouseY, partialTicks);
        }
        Client.INSTANCE.skia.endFrame();
    }

    @Override
    protected void input(float mouseX, float mouseY, EventInput event) {
        for (EleCategory category : categories) {
            category.input(mouseX, mouseY, event);
        }
    }
}
