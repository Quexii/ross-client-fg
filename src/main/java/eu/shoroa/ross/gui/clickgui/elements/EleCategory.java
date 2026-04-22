package eu.shoroa.ross.gui.clickgui.elements;

import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.gui.GuiElement;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import io.github.humbleui.skija.Color;

import static eu.shoroa.ross.Client.mc;

public class EleCategory extends GuiElement {
    private final Category category;

    private final Animate hoverAnimate = new Animate(150, Easing.CUBIC_IN_OUT);

    public EleCategory(Category category) {
        super(0f, 0f, 220f, 40f);
        this.category = category;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        hoverAnimate.doEase(getBounds().contains(mouseX, mouseY));
        Renderer.drawRRect(getX(), getY(), getWidth(), getHeight(), 5f, p -> {
            p.setStroke(true);
            p.setColor(Color.makeLerp(0x60222222, 0x60aaaaaa, hoverAnimate.getLinearValue()));
        });
        Renderer.drawFilter(Filter.kawase(), mc.getFramebuffer().framebufferTexture, getX(), getY(), getWidth(), getHeight(), 6f);
        Renderer.drawRRect(getX(), getY(), getWidth(), getHeight(), 6f, p -> {
            p.setColor(Color.makeLerp(0x40222222, 0x40aaaaaa, hoverAnimate.getLinearValue()));
        });
        Renderer.drawText(category.name(), getX() + getWidth() / 2f, getY() + getHeight() / 2f, Fonts.MirandaSans.weight(VariableFont.Weight.BLACK), 18f, Font.Align.CENTER, p -> {
            p.setColor(0xffffffff);
        });
    }

    @Override
    public void input(float mouseX, float mouseY, EventInput event) {

    }
}
