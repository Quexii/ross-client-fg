package eu.shoroa.ross.gui.clickgui.elements.bind;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.gui.GuiElement;
import eu.shoroa.ross.gui.clickgui.elements.bind.event.GuiEventSelectButton;
import eu.shoroa.ross.gui.elements.ButtonBase;
import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.types.Size;
import eu.shoroa.ross.util.render.MaterialIcons;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;

import java.util.Arrays;

public class EleKey extends ButtonBase {
    private final BindPanel.Btn btn;

    public EleKey(float x, float y, float width, float height, BindPanel.Btn btn) {
        super(x, y, width, height);
        this.btn = btn;

        hoverAnimation.setDuration(150L);
        pressAnimation.setDuration(100L);

        Action(() -> {
            Client.EVENT_BUS.post(new GuiEventSelectButton(this, btn));
        });
    }

    @Override
    protected void renderButton(float mouseX, float mouseY, float partialTicks) {
        if (hoverAnimation.canEase()) {
            hoverAnimation.forceFinish();
        }

        int modules = Math.toIntExact(Arrays.stream(ModuleManager.getModules()).filter(m -> m.bind != null && m.bind.type == btn.type && m.bind.key == btn.code).count());

        final float radius = 8f;
        final int accent = 0xFFe44c8a;
        final int accentDarker = 0xFFb23a6b;

        int text = 0xFF000000;
        int background = Color.makeLerp(0xFFDADADA, 0xFFEEEEEE, modules != 0 ? 1f : (float) hoverAnimation.getValue());
        int stroke = Color.makeLerp(0xFF999999, accent, (float) hoverAnimation.getValue());
        int depth = Color.makeLerp(0xFF888888, accentDarker, (float) hoverAnimation.getValue());

        float press = (float) (pressAnimation.getValue() * 4f);

        try (Paint p = new Paint()) {
            p.setColor(depth);
            Renderer.drawRRect(getX(), getY() + press, getWidth(), getHeight() + 4 - press, radius, p);

            p.setColor(stroke);
            Renderer.drawRRect(getX(), getY() + press, getWidth(), getHeight(), radius, p);

            float offset = 2f;
            p.setColor(background);
            Renderer.drawRRect(getX() + offset, getY() + offset + press, getWidth() - offset * 2, getHeight() - offset * 2, radius - offset, p);

            Font font;
            float textSize;
            if (btn.display.startsWith("i:")) {
                font = Fonts.MaterialIcons.weight(600).opticSize(24);
                textSize = 16f;
            } else {
                font = Fonts.GoogleFlex.weight(600);
                textSize = 14f;
            }

            String display = btn.display;
            if (display.startsWith("i:")) {
                display = display.substring(2);
            }

            Size textBounds = Renderer.getTextBounds(display, font, textSize);
            if (Math.abs(textBounds.width - getWidth()) < 10) {
                textSize = 13f;
                font = ((VariableFont.DerivedFont)font).weight(700);
            }

            p.setColor(text);
            Renderer.drawText(display, getX() + getWidth() / 2f, getY() + getHeight() / 2f + press, font, textSize, Font.Align.CENTER, p);
//            Renderer.drawText("" + (Math.abs(getWidth() - textBounds.width)), getX() + getWidth() / 2f, getY() + getHeight() / 2f, Fonts.MarketDeco, textSize, Font.Align.CENTER, p);
        }
    }
}
