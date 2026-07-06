package eu.shoroa.ross.feature.gui.clickgui.stella.elements.settings;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.utils.math.Mth;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.animate.Easing;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;

public class StellaBooleanSetting extends StellaSetting<Boolean> {
    private static final float PILL_W = 110f;
    private static final float PILL_H = 36f;

    private final Animate toggleEase = new Animate(150L, Easing.CIRC_IN_OUT);

    public StellaBooleanSetting(float x, float y, float width, float height, BooleanSetting setting) {
        super(x, y, width, height, setting);
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        toggleEase.doEase(getSetting().get());
        float t = (float) toggleEase.getValue();

        float px = getX() + getWidth() - PILL_W;
        float py = getY() + (getHeight() - PILL_H) / 2f;

        try (Paint p = new Paint()) {
            drawLabel(p);

            p.setColor(Color.makeLerp(theme().inactive, theme().accent, t));
            UI.drawRRect(px, py, PILL_W, PILL_H, PILL_H / 2f, p);

            p.setStroke(true);
            p.setStrokeWidth(1f);
            p.setColor(theme().outline);
            UI.drawRRect(px, py, PILL_W, PILL_H, PILL_H / 2f, p);
            p.setStroke(false);

            p.setColor(Color.withA(theme().onAccent, (int) (255 * t)));
            UI.drawText("On", px + 24f, py + PILL_H / 2f, Fonts.GoogleFlex.weight(600), 17f, Align.CENTER_LEFT, p);
            p.setColor(Color.withA(theme().onAccent, (int) (255 * (1 - t))));
            UI.drawText("Off", px + PILL_W - 24f, py + PILL_H / 2f, Fonts.GoogleFlex.weight(600), 17f, Align.CENTER_RIGHT, p);

            float knobR = PILL_H / 2f - 4f;
            float knobX = Mth.lerp(px + 4f + knobR, px + PILL_W - 4f - knobR, t);
            p.setColor(theme().surfaceBright);
            UI.drawCircle(knobX, py + PILL_H / 2f, knobR, p);
            p.setStroke(true);
            p.setStrokeWidth(1f);
            p.setColor(theme().shadowSoft);
            UI.drawCircle(knobX, py + PILL_H / 2f, knobR, p);
            p.setStroke(false);
        }
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (event.type == EventInput.Type.MOUSE && event.action == EventInput.Action.PRESS && event.value == 0) {
            float px = getX() + getWidth() - PILL_W;
            float py = getY() + (getHeight() - PILL_H) / 2f;
            if (mouseX >= px && mouseX <= px + PILL_W && mouseY >= py && mouseY <= py + PILL_H) {
                ((BooleanSetting) getSetting()).toggle();
                return true;
            }
        }
        return false;
    }
}
