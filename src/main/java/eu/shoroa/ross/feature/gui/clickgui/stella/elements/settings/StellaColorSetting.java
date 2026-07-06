package eu.shoroa.ross.feature.gui.clickgui.stella.elements.settings;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.setting.ColorSetting;
import eu.shoroa.ross.utils.math.Mth;
import eu.shoroa.ross.render.ui.UI;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Shader;

import java.awt.Color;

public class StellaColorSetting extends StellaSetting<Color> {
    private static final float HUE_W = 150f;
    private static final float ALPHA_W = 70f;
    private static final float SLIDER_H = 12f;
    private static final float KNOB_R = 9f;
    private static final float SWATCH = 36f;
    private static final float GAP = 16f;

    private static final int[] HUE_COLORS = {
            0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000
    };

    private boolean draggingHue = false;
    private boolean draggingAlpha = false;

    public StellaColorSetting(float x, float y, float width, float height, ColorSetting setting) {
        super(x, y, width, height, setting);
    }

    private float swatchX() {
        return getX() + getWidth() - SWATCH;
    }

    private float alphaX() {
        return swatchX() - GAP - ALPHA_W;
    }

    private float hueX() {
        return alphaX() - GAP - HUE_W;
    }

    private float sliderY() {
        return getY() + (getHeight() - SLIDER_H) / 2f;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        Color color = getSetting().get();
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        if (draggingHue) {
            float hue = Mth.clamp((mouseX - hueX()) / HUE_W, 0f, 1f);
            Color base = Color.getHSBColor(hue, Math.max(hsb[1], 0.01f), Math.max(hsb[2], 0.01f));
            getSetting().set(new Color(base.getRed(), base.getGreen(), base.getBlue(), color.getAlpha()));
            color = getSetting().get();
            hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        }
        if (draggingAlpha) {
            int alpha = (int) (Mth.clamp((mouseX - alphaX()) / ALPHA_W, 0f, 1f) * 255);
            getSetting().set(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            color = getSetting().get();
        }

        float sy = sliderY();
        float cy = sy + SLIDER_H / 2f;
        int opaque = 0xFF000000 | (color.getRGB() & 0xFFFFFF);

        try (Paint p = new Paint()) {
            drawLabel(p);

            p.setShader(Shader.makeLinearGradient(hueX(), cy, hueX() + HUE_W, cy, HUE_COLORS));
            UI.drawRRect(hueX(), sy, HUE_W, SLIDER_H, SLIDER_H / 2f, p);
            p.setShader(null);
            drawKnob(p, hueX() + hsb[0] * HUE_W, cy);

            p.setColor(theme().track);
            UI.drawRRect(alphaX(), sy, ALPHA_W, SLIDER_H, SLIDER_H / 2f, p);
            p.setShader(Shader.makeLinearGradient(alphaX(), cy, alphaX() + ALPHA_W, cy, new int[]{0x00FFFFFF & opaque, opaque}));
            UI.drawRRect(alphaX(), sy, ALPHA_W, SLIDER_H, SLIDER_H / 2f, p);
            p.setShader(null);
            drawKnob(p, alphaX() + (color.getAlpha() / 255f) * ALPHA_W, cy);

            float sx = swatchX();
            float swy = getY() + (getHeight() - SWATCH) / 2f;
            p.setColor(color.getRGB());
            UI.drawRRect(sx, swy, SWATCH, SWATCH, 10f, p);
            p.setStroke(true);
            p.setStrokeWidth(1.5f);
            p.setColor(theme().border);
            UI.drawRRect(sx, swy, SWATCH, SWATCH, 10f, p);
            p.setStroke(false);
        }
    }

    private void drawKnob(Paint p, float x, float cy) {
        p.setColor(theme().surfaceBright);
        UI.drawCircle(x, cy, KNOB_R, p);
        p.setStroke(true);
        p.setStrokeWidth(1.5f);
        p.setColor(theme().border);
        UI.drawCircle(x, cy, KNOB_R, p);
        p.setStroke(false);
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (event.type != EventInput.Type.MOUSE || event.value != 0) return false;

        if (event.action == EventInput.Action.PRESS) {
            float cy = sliderY() + SLIDER_H / 2f;
            if (mouseY >= cy - KNOB_R - 4f && mouseY <= cy + KNOB_R + 4f) {
                if (mouseX >= hueX() - KNOB_R && mouseX <= hueX() + HUE_W + KNOB_R) {
                    draggingHue = true;
                    return true;
                }
                if (mouseX >= alphaX() - KNOB_R && mouseX <= alphaX() + ALPHA_W + KNOB_R) {
                    draggingAlpha = true;
                    return true;
                }
            }
        } else if (event.action == EventInput.Action.RELEASE && (draggingHue || draggingAlpha)) {
            draggingHue = false;
            draggingAlpha = false;
            return true;
        }
        return false;
    }
}
