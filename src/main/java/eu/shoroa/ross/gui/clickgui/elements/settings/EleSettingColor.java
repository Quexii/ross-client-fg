package eu.shoroa.ross.gui.clickgui.elements.settings;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.settings.ColorSetting;
import eu.shoroa.ross.types.Rect;
import io.github.humbleui.skija.FilterTileMode;
import io.github.humbleui.skija.GradientStyle;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Shader;
import io.github.humbleui.types.RRect;

import java.awt.*;


public class EleSettingColor extends EleSetting<Color> {
    private final Animate hoverAnimate = new Animate(150, Easing.CUBIC_IN_OUT);
    private final Animate expandAnimate = new Animate(150, Easing.CUBIC_IN_OUT);

    private boolean expanded = false;
    private boolean draggingHue = false;
    private boolean draggingSB = false;
    private boolean draggingAlpha = false;

    private float hue = 0;
    private float saturation = 0;
    private float brightness = 0;
    private float alpha = 0;

    public EleSettingColor(ColorSetting setting) {
        super(setting);

        final float[] hsb = Color.RGBtoHSB(getSetting().get().getRed(), getSetting().get().getGreen(), getSetting().get().getBlue(), null);
        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];
        alpha = getSetting().get().getAlpha() / 255f;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        final float previewSize = 20;
        Rect preview = new Rect(getX() + getWidth() - 32, getY() + getHeight() / 2f - previewSize / 2, previewSize, previewSize);

        final Rect svRect = new Rect(getX() + 10, getY() + getHeight() + 10, getWidth() - 60, 130f);
        final Rect alphaRect = new Rect(getX() + getWidth() - 40, getY() + getHeight() + 10, 10, 130f);
        final Rect hueRect = new Rect(getX() + getWidth() - 20, getY() + getHeight() + 10, 10, 130f);

        final float radius = 4f;

        // input
        if (draggingHue) {
            hue = (mouseY - (hueRect.y + 1f)) / (hueRect.height - 2f);
            hue = clamp01(hue);
        }

        if (draggingSB) {
            saturation = (mouseX - (svRect.x + 1f)) / (svRect.width - 2f);
            brightness = 1f - (mouseY - (svRect.y + 1f)) / (svRect.height - 2f);
            saturation = clamp01(saturation);
            brightness = clamp01(brightness);
        }

        if (draggingAlpha) {
            // Top = opaque, bottom = transparent to match the alpha gradient.
            alpha = 1f - (mouseY - (alphaRect.y + 1f)) / (alphaRect.height - 2f);
            alpha = clamp01(alpha);
        }

        syncSettingColor();

        hoverAnimate.doEase(getBounds().contains(mouseX, mouseY));
        expandAnimate.doEase(expanded);
        if (getBounds().contains(mouseX, mouseY)) hoverAnimate.forceFinish();

        try (Paint p = new Paint()) {
            p.setColor(0xffe2e2e2);
            Renderer.drawText(getSetting().getName(), getX() + 12, getY() + getHeight() / 2f, Fonts.GoogleFlex.weight(VariableFont.Weight.NORMAL), 14f, Font.Align.CENTER_LEFT, p);

            p.setStroke(true);
            p.setColor(0xffa9a9a9);
            Renderer.drawRRect(preview.x, preview.y, preview.width, preview.height, 6f, p);

            p.setStroke(false);
            p.setColor(0xff121212);
            Renderer.drawRRect(preview.x, preview.y, preview.width, preview.height, 6f, p);

            p.setColor(getSetting().get().getRGB());
            Renderer.drawRRect(preview.x + 1, preview.y + 1, preview.width - 2, preview.height - 2, 5f, p);
        }

        Client.INSTANCE.skia.getCanvas().save();
        Client.INSTANCE.skia.getCanvas().clipRRect(RRect.makeXYWH(getX(), getY() + getHeight(), getWidth(), 141 * (float) expandAnimate.getValue(), 6f), true);
        try (Paint p = new Paint()) {
            p.setColor(0x20FFFFFF);
            Renderer.drawRect(getX() + 10, getY() + getHeight(), getWidth() - 20, 1f, p);

            p.setStroke(true);
            p.setColor(0xffa9a9a9);
            Renderer.drawRRect(svRect.x, svRect.y, svRect.width, svRect.height, radius, p);
            Renderer.drawRRect(hueRect.x, hueRect.y, hueRect.width, hueRect.height, radius, p);
            Renderer.drawRRect(alphaRect.x, alphaRect.y, alphaRect.width, alphaRect.height, radius, p);

            p.setStroke(false);
            p.setColor(0xff121212);
            Renderer.drawRRect(svRect.x, svRect.y, svRect.width, svRect.height, radius, p);
            Renderer.drawRRect(hueRect.x, hueRect.y, hueRect.width, hueRect.height, radius, p);
            Renderer.drawRRect(alphaRect.x, alphaRect.y, alphaRect.width, alphaRect.height, radius, p);

            // hue fill
            p.setShader(Shader.makeLinearGradient(hueRect.x, hueRect.y + 1, hueRect.x, hueRect.y + hueRect.height - 2,
                    new int[]{
                            Color.HSBtoRGB(0, 1, 1),
                            Color.HSBtoRGB(0.166f, 1, 1),
                            Color.HSBtoRGB(0.333f, 1, 1),
                            Color.HSBtoRGB(0.5f, 1, 1),
                            Color.HSBtoRGB(0.666f, 1, 1),
                            Color.HSBtoRGB(0.833f, 1, 1),
                            Color.HSBtoRGB(1, 1, 1)
                    }, null, GradientStyle.DEFAULT));
            Renderer.drawRRect(hueRect.x + 1, hueRect.y + 1, hueRect.width - 2, hueRect.height - 2, radius - 1, p);

            // alpha fill
            p.setShader(Shader.makeLinearGradient(alphaRect.x, alphaRect.y + 1, alphaRect.x, alphaRect.y + alphaRect.height - 2,
                    new int[]{
                            (getSetting().get().getRGB() & 0x00FFFFFF) | 0xFF000000,
                            (getSetting().get().getRGB() & 0x00FFFFFF) | 0x00000000
                    }, null, GradientStyle.DEFAULT));
            Renderer.drawRRect(alphaRect.x + 1, alphaRect.y + 1, alphaRect.width - 2, alphaRect.height - 2, radius - 1, p);

            // saturation/value fill
            p.setShader(Shader.makeLinearGradient(svRect.x + 1, svRect.y + 1, svRect.x + svRect.width - 2, svRect.y + 1,
                    new int[]{
                            Color.HSBtoRGB(hue, 0, 1),
                            Color.HSBtoRGB(hue, 1, 1)
                    }, null, GradientStyle.DEFAULT));
            Renderer.drawRRect(svRect.x + 1, svRect.y + 1, svRect.width - 2, svRect.height - 2, radius - 1, p);

            p.setShader(Shader.makeLinearGradient(svRect.x + 1, svRect.y + 1, svRect.x + 1, svRect.y + svRect.height - 2,
                    new int[]{
                            0x00000000,
                            0xFF000000
                    }, null, GradientStyle.DEFAULT));
            Renderer.drawRRect(svRect.x + 1, svRect.y + 1, svRect.width - 2, svRect.height - 2, radius - 1, p);
        }

        try (Paint p = new Paint()) {
            p.setColor(0xFFFFFFFF);
            Renderer.drawRRect(hueRect.x - 1, hueRect.y - 2 + hue * (hueRect.height - 2), hueRect.width + 2, 4, radius - 1, p);

            Renderer.drawRRect(alphaRect.x - 1, alphaRect.y - 2 + (1f - alpha) * (alphaRect.height - 2), alphaRect.width + 2, 4, radius - 1, p);

            p.setStroke(true);
            p.setColor(0xffa9a9a9);
            Renderer.drawRRect(svRect.x + saturation * svRect.width - 4, svRect.y + (1 - brightness) * svRect.height - 4, 8, 8, radius, p);
            p.setStroke(false);
            p.setColor(0xff121212);
            Renderer.drawRRect(svRect.x + saturation * svRect.width - 4, svRect.y + (1 - brightness) * svRect.height - 4, 8, 8, radius, p);

            p.setColor(getSetting().get().getRGB());
            Renderer.drawRRect(svRect.x + saturation * svRect.width - 3, svRect.y + (1 - brightness) * svRect.height - 3, 6, 6, radius - 1, p);
        }

        Client.INSTANCE.skia.getCanvas().restore();
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        final Rect svRect = new Rect(getX() + 10, getY() + getHeight() + 10, getWidth() - 60, 130f);
        final Rect alphaRect = new Rect(getX() + getWidth() - 40, getY() + getHeight() + 10, 10, 130f);
        final Rect hueRect = new Rect(getX() + getWidth() - 20, getY() + getHeight() + 10, 10, 130f);

        if (getBounds().contains(mouseX, mouseY) && event.action == EventInput.Action.PRESS && event.type == EventInput.Type.MOUSE && event.value == 1) {
            expanded = !expanded;
            return true;
        }
        if (expanded) {
            if (event.action == EventInput.Action.PRESS && event.type == EventInput.Type.MOUSE && event.value == 0) {
                if (svRect.contains(mouseX, mouseY)) {
                    draggingSB = true;
                    return true;
                }
                if (alphaRect.contains(mouseX, mouseY)) {
                    draggingAlpha = true;
                    return true;
                }
                if (hueRect.contains(mouseX, mouseY)) {
                    draggingHue = true;
                    return true;
                }
            } else if (event.action == EventInput.Action.RELEASE && event.type == EventInput.Type.MOUSE && event.value == 0 && (draggingSB || draggingAlpha || draggingHue)) {
                draggingSB = false;
                draggingAlpha = false;
                draggingHue = false;
                return true;
            }
        }
        return false;
    }

    private float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private void syncSettingColor() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int a = Math.round(clamp01(alpha) * 255f);
        getSetting().set(new Color(r, g, b, a));
    }

    @Override
    public float getExtendHeight() {
        return (float) (getHeight() + 140 * expandAnimate.getValue());
    }
}
