package eu.shoroa.ross.feature.gui.clickgui.stella.elements.settings;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.setting.ColorSetting;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.DampFloat;
import eu.shoroa.ross.utils.math.Mth;
import io.github.humbleui.skija.FilterBlurMode;
import io.github.humbleui.skija.MaskFilter;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Shader;
import org.lwjgl.input.Mouse;

import java.awt.Color;

public class StellaColorSetting extends StellaSetting<Color> {
    private static final float PAD = 6f;
    private static final float GAP = 8f;
    private static final float HUE_W = 12f;
    private static final float TRACK_H = 8f;
    private static final float KNOB_R = 8f;
    private static final float SWATCH = 22f;

    private static final int[] HUE_STOPS = {
            0xFFFF0000, 0xFFFFFF00, 0xFF00FF00,
            0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000
    };

    private boolean dragSV, dragHue, dragAlpha;
    private final DampFloat hueEase = new DampFloat();
    private final DampFloat alphaEase = new DampFloat();
    private final DampFloat svXEase = new DampFloat();
    private final DampFloat svYEase = new DampFloat();

    public StellaColorSetting(float x, float y, float width, float height, ColorSetting setting) {
        super(x, y, width, height, setting);
    }

    private ColorSetting setting() {
        return (ColorSetting) getSetting();
    }

    // ---- Layout ----

    private float svSize() {
        return Math.min(getWidth() * 0.42f, getHeight() - TRACK_H - GAP - PAD * 2);
    }

    private float svX() {
        return getX() + getWidth() - svSize() - GAP - PAD * 2 - SWATCH;
    }

    private float svY() {
        return getY() + PAD;
    }

    private float hueX() {
        return svX() + svSize() + GAP;
    }

    private float hueY() {
        return svY();
    }

    private float hueH() {
        return svSize();
    }

    private float alphaX() {
        return svX();
    }

    private float alphaY() {
        return svY() + svSize() + GAP;
    }

    private float alphaW() {
        return svSize() + GAP + HUE_W;
    }

    private float previewX() {
        return hueX() + HUE_W + GAP;
    }

    private float previewY() {
        return svY();
    }

    // ---- Render ----

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        ColorSetting cs = setting();
        Color color = cs.get();
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);

        if (dragSV || dragHue || dragAlpha) {
            if (!Mouse.isButtonDown(0)) {
                dragSV = dragHue = dragAlpha = false;
            }
        }

        if (dragSV) {
            float s = Mth.clamp((mouseX - svX()) / svSize(), 0f, 1f);
            float v = Mth.clamp(1f - (mouseY - svY()) / svSize(), 0f, 1f);
            Color base = Color.getHSBColor(hsb[0], s, v);
            cs.set(new Color(base.getRed(), base.getGreen(), base.getBlue(), color.getAlpha()));
            color = cs.get();
            hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        }
        if (dragHue) {
            float hue = Mth.clamp((mouseY - hueY()) / hueH(), 0f, 1f);
            Color base = Color.getHSBColor(hue, Math.max(hsb[1], 0.01f), Math.max(hsb[2], 0.01f));
            cs.set(new Color(base.getRed(), base.getGreen(), base.getBlue(), color.getAlpha()));
            color = cs.get();
            hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        }
        if (dragAlpha) {
            int alpha = (int) (Mth.clamp((mouseX - alphaX()) / alphaW(), 0f, 1f) * 255);
            cs.set(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
            color = cs.get();
        }

        // Smooth eased positions (matches NumberSetting slider feel)
        Mth.smoothDamp(hueEase, hsb[0], 0.15f, (float) Animate.getDelta());
        Mth.smoothDamp(alphaEase, color.getAlpha() / 255f, 0.15f, (float) Animate.getDelta());
        Mth.smoothDamp(svXEase, hsb[1], 0.05f, (float) Animate.getDelta());
        Mth.smoothDamp(svYEase, 1f - hsb[2], 0.05f, (float) Animate.getDelta());

        int opaque = 0xFF000000 | (color.getRGB() & 0xFFFFFF);
        int pureHue = Color.getHSBColor(hsb[0], 1f, 1f).getRGB() | 0xFF000000;

        try (Paint p = new Paint()) {
            drawLabel(p);

            // Hex value (mirrors NumberSetting value text placement)
            p.setColor(theme().foreground);
            UI.drawText(cs.toHexString(), alphaX() - GAP, alphaY() + TRACK_H / 2f,
                    Fonts.GoogleFlex.weight(600), 13f, Align.CENTER_RIGHT, p);

            // ===== SV Square =====
            UI.save();
            UI.clipRRect(svX(), svY(), svSize(), svSize(), 6f);
            p.setColor(pureHue);
            UI.drawRRect(svX(), svY(), svSize(), svSize(), 6f, p);

            // Saturation gradient (white → transparent)
            p.setColor(-1);
            p.setShader(Shader.makeLinearGradient(svX(), svY(), svX() + svSize(), svY(),
                    new int[]{0xFFFFFFFF, 0x00FFFFFF}));
            UI.drawRRect(svX(), svY(), svSize(), svSize(), 6f, p);
            p.setShader(null);

            // Value gradient (transparent → black)
            p.setShader(Shader.makeLinearGradient(svX(), svY(), svX(), svY() + svSize(),
                    new int[]{0x00000000, 0xFF000000}));
            UI.drawRRect(svX(), svY(), svSize(), svSize(), 6f, p);
            p.setShader(null);
            UI.restore();

            // Border
            p.setStroke(true);
            p.setStrokeWidth(2f);
            p.setColor(theme().surfaceRaised);
            UI.drawRRect(svX(), svY(), svSize(), svSize(), 6f, p);
            p.setStroke(false);

            // SV cursor (same knob style as NumberSetting)
            float cx = svX() + svXEase.value * svSize();
            float cy = svY() + svYEase.value * svSize();
            drawKnob(p, cx, cy);

            // ===== Hue Bar (vertical) =====
            p.setColor(-1);
            p.setShader(Shader.makeLinearGradient(hueX(), hueY() + hueH(), hueX(), hueY(), HUE_STOPS));
            UI.drawRRect(hueX(), hueY(), HUE_W, hueH(), HUE_W / 2f, p);
            p.setShader(null);

            p.setStroke(true);
            p.setStrokeWidth(1f);
            p.setColor(theme().border);
            UI.drawRRect(hueX(), hueY(), HUE_W, hueH(), HUE_W / 2f, p);
            p.setStroke(false);

            // Hue knob
            float hy = hueY() + hueEase.value * hueH();
            drawKnob(p, hueX() + HUE_W / 2f, hy);

            // ===== Alpha Slider =====
            UI.save();
            UI.clipRRect(alphaX(), alphaY(), alphaW(), TRACK_H, TRACK_H / 2f);
//            drawCheckerboard(alphaX(), alphaY() - 1, alphaW(), TRACK_H + 2, 0);

            p.setColor(-1);
            p.setShader(Shader.makeLinearGradient(alphaX(), alphaY(), alphaX() + alphaW(), alphaY(),
                    new int[]{0x00FFFFFF & opaque, opaque}));
            UI.drawRRect(alphaX(), alphaY(), alphaW(), TRACK_H, TRACK_H / 2f, p);
            p.setShader(null);

            p.setStroke(true);
            p.setStrokeWidth(3f);
            p.setColor(theme().surfaceRaised);
            UI.drawRRect(alphaX(), alphaY(), alphaW(), TRACK_H, TRACK_H / 2f, p);
            p.setStroke(false);
            UI.restore();

            // Alpha knob
            float ax = alphaX() + alphaEase.value * alphaW();
            drawKnob(p, ax, alphaY() + TRACK_H / 2f);

            // ===== Preview Swatch =====
            UI.save();
            UI.clipRRect(previewX(), previewY(), SWATCH, SWATCH, 6f);
//            drawCheckerboard(previewX(), previewY(), SWATCH, SWATCH, 0f);
            p.setColor(color.getRGB());
            UI.drawRRect(previewX(), previewY(), SWATCH, SWATCH, 6f, p);
            p.setStroke(true);
            p.setStrokeWidth(3f);
            p.setColor(theme().surfaceRaised);
            UI.drawRRect(previewX(), previewY(), SWATCH, SWATCH, 6f, p);
            p.setStroke(false);
            UI.restore();
        }
    }

    // ---- Knob style matching NumberSetting ----

    private void drawKnob(Paint p, float x, float y) {
        // Shadow blur
        p.setColor(theme().shadow);
        p.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 3f));
        UI.drawCircle(x, y + 1f, KNOB_R, p);
        p.setMaskFilter(null);

        // Surface fill
        p.setColor(theme().surface);
        UI.drawCircle(x, y, KNOB_R, p);

        // Border stroke
        p.setStroke(true);
        p.setStrokeWidth(1.5f);
        p.setColor(theme().border);
        UI.drawCircle(x, y, KNOB_R, p);
        p.setStroke(false);
    }

    // ---- Checkerboard for transparency ----

    private void drawCheckerboard(float x, float y, float w, float h, float radius) {
        try (Paint p = new Paint()) {
            p.setColor(0xFFFFFFFF);
            UI.drawRRect(x, y, w, h, radius, p);

            p.setColor(0xFFCCCCCC);
            float size = 5f;
            int cols = (int) Math.ceil(w / size);
            int rows = (int) Math.ceil(h / size);
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    if ((row + col) % 2 == 1) {
                        float cx = x + col * size;
                        float cy = y + row * size;
                        float cw = Math.min(size, x + w - cx);
                        float ch = Math.min(size, y + h - cy);
                        if (cw > 0 && ch > 0) {
                            UI.drawRRect(cx, cy, cw, ch, 0, p);
                        }
                    }
                }
            }
        }
    }

    // ---- Input ----

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (event.type != EventInput.Type.MOUSE || event.value != 0) return false;

        if (event.action == EventInput.Action.PRESS) {
            if (mouseX >= svX() && mouseX <= svX() + svSize() &&
                    mouseY >= svY() && mouseY <= svY() + svSize()) {
                dragSV = true;
                return true;
            }
            if (mouseX >= hueX() - KNOB_R && mouseX <= hueX() + HUE_W + KNOB_R &&
                    mouseY >= hueY() && mouseY <= hueY() + hueH()) {
                dragHue = true;
                return true;
            }
            if (mouseY >= alphaY() - KNOB_R && mouseY <= alphaY() + TRACK_H + KNOB_R &&
                    mouseX >= alphaX() && mouseX <= alphaX() + alphaW()) {
                dragAlpha = true;
                return true;
            }
        } else if (event.action == EventInput.Action.RELEASE) {
            if (dragSV || dragHue || dragAlpha) {
                dragSV = dragHue = dragAlpha = false;
                return true;
            }
        }
        return false;
    }
}