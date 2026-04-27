package eu.shoroa.ross.gui.clickgui.elements.settings;

import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.mixins.injection.client.MinecraftAccessor;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.settings.BooleanSetting;
import eu.shoroa.ross.settings.NumberSetting;
import eu.shoroa.ross.types.Rect;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.Minecraft;

import static eu.shoroa.ross.Client.mc;

public class EleSettingNumber extends EleSetting<Float> {

    private boolean isDragging = false;
    private float smoothRenderValue = 0f;

    private final Animate hoverAnimate = new Animate(150, Easing.CUBIC_IN_OUT);
    private final Animate dragAnimate = new Animate(150, Easing.CUBIC_IN_OUT);

    public EleSettingNumber(NumberSetting setting) {
        super(setting);

        setHeight(60);
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        hoverAnimate.doEase(getBounds().contains(mouseX, mouseY) || isDragging);
        dragAnimate.doEase(isDragging);

        float deltaTime = Minecraft.getDebugFPS() > 0 ? 1f / Minecraft.getDebugFPS() : 1f / 60f;

        float min = ((NumberSetting) getSetting()).getMin();
        float max = ((NumberSetting) getSetting()).getMax();
        float increment = ((NumberSetting) getSetting()).getIncrement();
        String valueString = String.format("%." + (increment < 1 ? String.valueOf(increment).length() - 2 : 0) + "f", getSetting().get());

        if (isDragging) {
            dragAnimate.forceFinish();

            float delta = (mouseX - (getX() + 10f)) / (getWidth() - 20f);
            delta = Math.max(0, Math.min(1, delta));

            if (increment < 1) {
                delta = Math.round(delta * (max - min) / increment) * increment / (max - min);
            }

            getSetting().set(min + (max - min) * delta);
        }

        try (Paint p = new Paint()) {
            p.setColor(0xffcccccc);
            VariableFont.DerivedFont font = Fonts.GoogleFlex
                    .weight(400)
                    .opticSize(14);

            Renderer.drawText(getSetting().getName(), getX() + 10, getY() + 20f, font, 14f, Font.Align.CENTER_LEFT, p);

            float renderValue = (getSetting().get() - min) / (max - min);
            smoothRenderValue += (renderValue - smoothRenderValue) * deltaTime * 40f;

            if (Float.isNaN(smoothRenderValue) || Float.isInfinite(smoothRenderValue)) smoothRenderValue = renderValue;

            float barWidth = (getWidth() - 20f) * smoothRenderValue;

            Renderer.drawText(valueString, getX() + getWidth() - 10, getY() + 20f, font, 14f, Font.Align.CENTER_RIGHT, p);

            p.setColor(0x35FFFFFF);
            Renderer.drawRRect(getX() + 10f, getY() + getHeight() - 22f, getWidth() - 20f, 6f, 3f, p);

            p.setColor(0x95FFFFFF);
            Renderer.drawRRect(getX() + 10f, getY() + getHeight() - 22f, barWidth, 6f, 3f, p);


            int steps = (int) ((max - min) / increment);
            if (steps > 0 && steps < 40) {
                for (int i = 0; i <= steps; i++) {
                    if (i == 0 || i == steps) continue;

                    float stepX = getX() + 10f + (getWidth() - 20f) * (i / (float) steps);

                    p.setStroke(false);
                    p.setColor(Color.makeLerp(0x00FFFFFF, 0x55FFFFFF, (float) hoverAnimate.getLinearValue()));
                    p.setAntiAlias(false);
                    Renderer.drawRect(stepX - 0.5f, getY() + getHeight() - 24f, 1f, 10f, p);
                }
            }

            float extend = (float) (dragAnimate.getValue() * 2f);

            p.setAntiAlias(true);
            p.setColor(Color.makeLerp(0x00FFFFFF, 0x88FFFFFF, (float) hoverAnimate.getLinearValue()));
            p.setColor(Color.makeLerp(p.getColor(), -1, (float) dragAnimate.getLinearValue()));
            Renderer.drawRRect(getX() + 10f + barWidth - 2f, getY() + getHeight() - 26f - extend, 4f, 14f + extend * 2f, 3f, p);
        }
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        Rect slider = new Rect(getX() + 10f, getY() + getHeight() - 22f - 8, getWidth() - 20f, 6f + 16);
        if (slider.contains(mouseX, mouseY) && event.action == EventInput.Action.PRESS && event.type == EventInput.Type.MOUSE && event.value == 0) {
            isDragging = true;
            return true;
        }
        if (isDragging && event.action == EventInput.Action.RELEASE && event.type == EventInput.Type.MOUSE && event.value == 0) {
            isDragging = false;
            return true;
        }
        return false;
    }

    @Override
    public float getExtendHeight() {
        return getHeight();
    }
}
