package eu.shoroa.ross.feature.gui.clickgui.stella.elements;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.gui.GuiElement;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.gui.clickgui.stella.events.StellaEvents;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.animate.Easing;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.skia.image.ImageSource;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.Images;
import eu.shoroa.ross.render.ui.UI;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.Rect;

import static eu.shoroa.ross.Client.EVENT_BUS;

public class StellaModule extends GuiElement {
    public final Module module;
    private boolean visible = false;

    private final Animate hoverEase = new Animate(150L, Easing.CIRC_OUT);
    private final Animate toggleEase = new Animate(150L, Easing.CIRC_OUT);

    public StellaModule(float x, float y, float width, float height, Module module) {
        super(x, y, width, height);
        this.module = module;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        boolean hovered = contains(mouseX, mouseY);
        hoverEase.doEase(hovered);
        toggleEase.doEase(module.isEnabled());

        float hover = (float) hoverEase.getLinearValue();
        float toggle = (float) toggleEase.getLinearValue();

        final float x = getX();
        final float y = getY();
        final float w = getWidth();
        final float h = getHeight();

        final boolean hasSettings = module.getSettings().size() > 0;

        final float buttonHeight = 40f;
        final float buttonGap = 8f;
        final float radius = 16f;

        int toggleColor = Color.makeLerp(StellaTheme.get().inactive, StellaTheme.get().accent, toggle);
        Canvas canvas = Client.INSTANCE.getSkia().getCanvas();
        ImageSource image = Images.HALFTONE_CIRCLE;
        float imageScale = 0.7f;
        float imageW = image.getWidth() * imageScale;
        float imageH = image.getHeight() * imageScale;

        try (Paint p = new Paint()) {
            p.setColor(StellaTheme.get().shadow);
            p.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 5f + hover * 4f));
            UI.drawRRect(x, y + 6f, w, h, radius, p);
            p.setMaskFilter(null);

            p.setColor(StellaTheme.get().surface);
            UI.drawRRect(x, y, w, h, radius, p);

            p.setStroke(true);
            p.setStrokeWidth(1.5f);
            p.setColor(StellaTheme.get().border);
            UI.drawRRect(x, y, w, h, radius, p);
            p.setStroke(false);

            p.setColor(StellaTheme.get().text);
            UI.drawText(module.icon, x + w / 2f, y + (h - buttonHeight) / 2f, Fonts.MaterialIcons.weight(350).fill(true), 52f, Align.CENTER, p);

            UI.save();
            UI.clipRRect(x, y, w, h, radius);
            p.setColorFilter(ColorFilter.makeBlend(Color.withA(StellaTheme.get().shadow, 15), BlendMode.SRC_IN));
            canvas.drawImageRect(image.getImage(), Rect.makeXYWH(x + (w - imageW) / 2, y + h - imageH / 2, imageW, imageH), p);
            p.setColorFilter(null);
            UI.restore();

            p.setColor(toggleColor);
            UI.drawRRect(x + buttonGap, y + h - buttonHeight - buttonGap, w - buttonGap * 2, buttonHeight, radius - buttonGap, p);
            p.setStroke(true);
            p.setStrokeWidth(1f);
            p.setColor(StellaTheme.get().outline);
            UI.drawRRect(x + buttonGap, y + h - buttonHeight - buttonGap, w - buttonGap * 2, buttonHeight, radius - buttonGap, p);
            p.setStroke(false);

            p.setColor(StellaTheme.get().onAccent);
            UI.drawText(module.name, x + w / 2f, y + h - buttonHeight / 2 - buttonGap, Fonts.GoogleFlex.weight(500), 18f, Align.CENTER, p);

            if (hasSettings) {
                UI.drawText(MaterialIcons.SETTINGS, x + w - 30f, y + h - buttonHeight / 2 - buttonGap, Fonts.MaterialIcons.weight(400).opticSize(48).fill(true), 24f, Align.CENTER, p);
            }
        }
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        final float buttonHeight = 40f;
        final float buttonGap = 8f;
        final boolean hasSettings = module.getSettings().size() > 0;
        final float settingsOffset = hasSettings ? 40f : 0f;

        boolean buttonHovered = mouseX >= getX() + buttonGap && mouseX <= getX() + getWidth() - buttonGap - settingsOffset &&
                mouseY >= getY() + getHeight() - buttonHeight - buttonGap && mouseY <= getY() + getHeight() - buttonGap;

        boolean settingsHovered = hasSettings && mouseX >= getX() + getWidth() - settingsOffset - buttonGap && mouseX <= getX() + getWidth() - buttonGap &&
                mouseY >= getY() + getHeight() - buttonHeight - buttonGap && mouseY <= getY() + getHeight() - buttonGap;

        if (event.type == EventInput.Type.MOUSE && event.action == EventInput.Action.PRESS) {
            if (event.value == 0) {
                if (buttonHovered) {
                    module.toggle();
                    return true;
                }
                if (settingsHovered) {
                    EVENT_BUS.post(new StellaEvents.Popup(new StellaPopupSettings(800, 500, module)));
                    return true;
                }
            }
        }
        return false;
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public Module getModule() {
        return module;
    }

    public boolean isVisible() {
        return visible;
    }
}