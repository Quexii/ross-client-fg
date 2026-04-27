package eu.shoroa.ross.gui.clickgui.elements.settings;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.settings.ColorSetting;
import io.github.humbleui.skija.Paint;

import java.awt.*;


public class EleSettingColor extends EleSetting<Color> {
    public EleSettingColor(ColorSetting setting) {
        super(setting);
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        try (Paint p = new Paint()) {
            p.setColor(0xffe2e2e2);
            Renderer.drawText(getSetting().getName(), getX() + 12, getY() + getHeight() / 2f, Fonts.GoogleFlex.weight(VariableFont.Weight.NORMAL), 14f, Font.Align.CENTER_LEFT, p);
        }
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        return false;
    }

    @Override
    public float getExtendHeight() {
        return getHeight();
    }
}
