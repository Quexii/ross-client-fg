package eu.shoroa.ross.feature.gui.clickgui.stella.elements;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.gui.GuiElement;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.gui.clickgui.stella.events.StellaEvents;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.skia.image.ImageSource;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.Images;
import eu.shoroa.ross.render.ui.UI;
import io.github.humbleui.skija.BlendMode;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.ColorFilter;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;

import static eu.shoroa.ross.Client.EVENT_BUS;

public abstract class StellaPopup extends GuiElement {
    public final String title;

    private static final float CLOSE_SIZE = 36f;

    public StellaPopup(float width, float height, String title) {
        super(0, 0, width, height);
        this.title = title;
    }

    abstract void content(float mouseX, float mouseY, float partialTicks);

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        Canvas canvas = Client.INSTANCE.getSkia().getCanvas();

        ImageSource image = Images.HALFTONE_CIRCLE;
        float imageScale = 0.7f;
        float imageW = image.getWidth() * imageScale;
        float imageH = image.getHeight() * imageScale;

//        halftone color: #FF0CB9BF
        try (Paint p = new Paint()) {
            p.setColor(StellaTheme.get().surface);
            UI.drawRRect(getX(), getY(), getWidth(), getHeight(), 4f, p);

            p.setColor(StellaTheme.get().accent);
            UI.drawRRect(getX() + 10f, getY() + 10f, getWidth() - 20f, 60f, 2f, p);

            UI.save();
            UI.clipRRect(getX() + 10f, getY() + 10f, getWidth() - 20f, 60f, 2f);
            p.setColorFilter(ColorFilter.makeBlend(StellaTheme.get().accentHalftone, BlendMode.SRC_IN));
            canvas.drawImageRect(Images.HALFTONE_CIRCLE.getImage(), Rect.makeXYWH(getX() - imageW / 3, getY() - imageH / 3, imageW, imageH), p);
            p.setColorFilter(null);
            UI.restore();

            p.setColor(StellaTheme.get().text);
            UI.drawText(title, getX() + 54f, getY() + 42f, Fonts.GoogleFlex.weight(550), 24f, Align.CENTER_LEFT, p);
            UI.drawText(MaterialIcons.LOCATION_ON, getX() + 20f, getY() + 42f, Fonts.MaterialIcons.weight(800).opticSize(24).grade(200).fill(true), 24f, Align.CENTER_LEFT, p);

            p.setColor(StellaTheme.get().onAccent);
            UI.drawText(title, getX() + 54f, getY() + 40f, Fonts.GoogleFlex.weight(550), 24f, Align.CENTER_LEFT, p);
            UI.drawText(MaterialIcons.LOCATION_ON, getX() + 20f, getY() + 40f, Fonts.MaterialIcons.weight(800).opticSize(24).grade(200).fill(true), 24f, Align.CENTER_LEFT, p);

            float cx = closeX();
            float cy = closeY();
            p.setColor(StellaTheme.get().text);
            UI.drawRRect(cx, cy, CLOSE_SIZE, CLOSE_SIZE, 4f, p);
            p.setColor(StellaTheme.get().onAccent);
            UI.drawText(MaterialIcons.CLOSE, cx + CLOSE_SIZE / 2f, cy + CLOSE_SIZE / 2f, Fonts.MaterialIcons.weight(700).grade(200), 26f, Align.CENTER, p);
        }

        content(mouseX, mouseY, partialTicks);
    }

    private float closeX() {
        return getX() + getWidth() - 22f - CLOSE_SIZE;
    }

    private float closeY() {
        return getY() + 22f;
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (event.type == EventInput.Type.MOUSE && event.action == EventInput.Action.PRESS && event.value == 0) {
            float cx = closeX();
            float cy = closeY();
            if (mouseX >= cx && mouseX <= cx + CLOSE_SIZE && mouseY >= cy && mouseY <= cy + CLOSE_SIZE) {
                EVENT_BUS.post(new StellaEvents.ClosePopup());
                return true;
            }
        }
        return false;
    }
}
