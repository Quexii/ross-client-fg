package eu.shoroa.ross.gui.clickgui.elements.settings;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.settings.BooleanSetting;
import eu.shoroa.ross.util.render.MaterialIcons;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;

public class EleSettingBoolean extends EleSetting<Boolean> {
    private final Animate hoverAnimate = new Animate(150, Easing.CUBIC_IN_OUT);
    private final Animate toggleAnimate = new Animate(150, Easing.CUBIC_IN_OUT);

    public EleSettingBoolean(BooleanSetting setting) {
        super(setting);
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        hoverAnimate.doEase(getBounds().contains(mouseX, mouseY));
        toggleAnimate.doEase(getSetting().get());

        Canvas canvas = Client.INSTANCE.skia.getCanvas();

        try (Paint p = new Paint()) {
            p.setColor(0xffcccccc);
            VariableFont.DerivedFont font = Fonts.GoogleFlex
                    .weight(400)
                    .opticSize(14);

            Renderer.drawText(getSetting().getName(), getX() + 10, getY() + getHeight() / 2f, font, 14f, Font.Align.CENTER_LEFT, p);

            p.setColor(0x10FFFFFF);
            Renderer.drawRRect(getX() + getWidth() - 30, getY() + getHeight() / 2f - 8, 16, 16, 6, p);
            p.setColor(0x25FFFFFF);
            p.setStroke(true);
            p.setStrokeWidth(1.5f);
            Renderer.drawRRect(getX() + getWidth() - 30, getY() + getHeight() / 2f - 8, 16, 16, 6, p);

            VariableFont.DerivedFont iconFont = Fonts.MaterialIcons.weight(200);

            p.setColor(0xFF4caf50);
            canvas.save();
            canvas.translate(getX() + getWidth() - 22, getY() + getHeight() / 2f);
            canvas.scale((float) toggleAnimate.getLinearValue(), (float) toggleAnimate.getLinearValue());
            canvas.translate(-(getX() + getWidth() - 22), -(getY() + getHeight() / 2f));
            Renderer.drawText(MaterialIcons.CHECK, getX() + getWidth() - 22, getY() + getHeight() / 2f, iconFont, 14f, Font.Align.CENTER, p);
            canvas.restore();

            p.setColor(0xFFd32f2f);
            canvas.save();
            canvas.translate(getX() + getWidth() - 22, getY() + getHeight() / 2f);
            canvas.scale(1f - (float) toggleAnimate.getLinearValue(), 1f - (float) toggleAnimate.getLinearValue());
            canvas.translate(-(getX() + getWidth() - 22), -(getY() + getHeight() / 2f));
            Renderer.drawText(MaterialIcons.CLOSE, getX() + getWidth() - 22, getY() + getHeight() / 2f, iconFont, 14f, Font.Align.CENTER, p);
            canvas.restore();
        }
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (getBounds().contains(mouseX, mouseY) && event.action == EventInput.Action.PRESS && event.type == EventInput.Type.MOUSE) {
            if (event.value == 0) getSetting().set(!getSetting().get());
            return true;
        }
        return false;
    }

    @Override
    public float getExtendHeight() {
        return getHeight();
    }
}
