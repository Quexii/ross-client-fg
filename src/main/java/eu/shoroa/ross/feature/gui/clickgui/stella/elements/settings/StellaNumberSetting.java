package eu.shoroa.ross.feature.gui.clickgui.stella.elements.settings;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.utils.math.Mth;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.DampFloat;
import io.github.humbleui.skija.FilterBlurMode;
import io.github.humbleui.skija.MaskFilter;
import io.github.humbleui.skija.Paint;

public class StellaNumberSetting extends StellaSetting<Float> {
    private static final float TRACK_W = 240f;
    private static final float TRACK_H = 8f;
    private static final float KNOB_R = 10f;

    private DampFloat sliderEase = new DampFloat();

    private boolean dragging = false;

    public StellaNumberSetting(float x, float y, float width, float height, NumberSetting setting) {
        super(x, y, width, height, setting);
    }

    private NumberSetting setting() {
        return (NumberSetting) getSetting();
    }

    private float trackX() {
        return getX() + getWidth() - TRACK_W - KNOB_R;
    }

    private float trackY() {
        return getY() + (getHeight() - TRACK_H) / 2f;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        NumberSetting setting = setting();

        if (dragging) {
            float frac = Mth.clamp((mouseX - trackX()) / TRACK_W, 0f, 1f);
            setting.set(setting.getMin() + frac * (setting.getMax() - setting.getMin()));
        }

        float frac = (setting.get() - setting.getMin()) / (setting.getMax() - setting.getMin());

        Mth.smoothDamp(sliderEase, frac, 0.1f, (float) Animate.getDelta());
        float tx = trackX();
        float ty = trackY();
        float knobX = tx + sliderEase.value * TRACK_W;
        float cy = ty + TRACK_H / 2f - 1;

        try (Paint p = new Paint()) {
            drawLabel(p);

            p.setColor(theme().foreground);
            UI.drawText(format(setting.get()), tx - 20f, getY() + getHeight() / 2f, Fonts.GoogleFlex.weight(600), 20f, Align.CENTER_RIGHT, p);

            p.setColor(theme().track);
            UI.drawRRect(tx, ty, TRACK_W, TRACK_H, TRACK_H / 2f, p);
            p.setColor(theme().accent);
            UI.drawRRect(tx, ty, Math.max(TRACK_H, sliderEase.value * TRACK_W), TRACK_H, TRACK_H / 2f, p);

            p.setColor(theme().shadow);
            p.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 3f));
            UI.drawCircle(knobX, cy + 2f, KNOB_R, p);
            p.setMaskFilter(null);

            p.setColor(theme().surface);
            UI.drawCircle(knobX, cy, KNOB_R, p);
            p.setStroke(true);
            p.setStrokeWidth(1.5f);
            p.setColor(theme().border);
            UI.drawCircle(knobX, cy, KNOB_R, p);
            p.setStroke(false);
        }
    }

    private String format(float value) {
        float increment = setting().getIncrement();
        if (increment >= 1f) return String.valueOf((int) value);
        if (increment >= 0.1f) return String.format("%.1f", value);
        return String.format("%.2f", value);
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (event.type != EventInput.Type.MOUSE || event.value != 0) return false;

        if (event.action == EventInput.Action.PRESS) {
            float tx = trackX();
            float cy = trackY() + TRACK_H / 2f;
            if (mouseX >= tx - KNOB_R && mouseX <= tx + TRACK_W + KNOB_R && mouseY >= cy - KNOB_R - 4f && mouseY <= cy + KNOB_R + 4f) {
                dragging = true;
                return true;
            }
        } else if (event.action == EventInput.Action.RELEASE && dragging) {
            dragging = false;
            return true;
        }
        return false;
    }
}
