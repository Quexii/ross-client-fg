package eu.shoroa.ross.feature.gui.clickgui.stella.elements.settings;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.setting.ModeEnum;
import eu.shoroa.ross.feature.setting.ModeSetting;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import io.github.humbleui.skija.Paint;

public class StellaModeSetting<T extends Enum<T> & ModeEnum> extends StellaSetting<T> {
    private static final float RADIO_SIZE = 28f;
    private static final float OPTION_GAP = 28f;
    private static final float ICON_TEXT_GAP = 10f;
    private static final float CYCLER_W = 190f;
    private static final float CYCLER_H = 38f;

    public StellaModeSetting(float x, float y, float width, float height, ModeSetting<T> setting) {
        super(x, y, width, height, setting);
    }

    private ModeSetting<T> setting() {
        return (ModeSetting<T>) getSetting();
    }

    private float optionWidth(T mode) {
        return RADIO_SIZE + ICON_TEXT_GAP + UI.getTextBounds(mode.displayName(), Fonts.GoogleFlex.weight(550), 18f).width;
    }

    private float optionsWidth() {
        float total = 0f;
        for (T mode : setting().getModes()) {
            total += optionWidth(mode) + OPTION_GAP;
        }
        return total - OPTION_GAP;
    }

    private boolean compact() {
        return optionsWidth() > getWidth() * 0.6f;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        try (Paint p = new Paint()) {
            drawLabel(p);

            if (compact()) {
                renderCycler(p);
            } else {
                renderRadios(p);
            }
        }
    }

    private void renderRadios(Paint p) {
        ModeSetting<T> setting = setting();
        float ox = getX() + getWidth() - optionsWidth();
        float cy = getY() + getHeight() / 2f;

        for (T mode : setting.getModes()) {
            boolean selected = mode == setting.getCurrent();
            float half = RADIO_SIZE / 2f;

            if (selected) {
                p.setColor(theme().accent);
                UI.drawCircle(ox + half, cy, half, p);
//                UI.drawRRect(ox, cy - half, RADIO_SIZE, RADIO_SIZE, 9f, p);
                p.setColor(theme().onAccent);
                UI.drawText(MaterialIcons.CHECK, ox + half, cy, Fonts.MaterialIcons.weight(700).grade(200).fill(true), 22f, Align.CENTER, p);
            } else {
                p.setColor(theme().radio);
                UI.drawCircle(ox + half, cy, half, p);
                p.setStroke(true);
                p.setStrokeWidth(1.5f);
                p.setColor(theme().radioBorder);
                UI.drawCircle(ox + half, cy, half, p);
                p.setStroke(false);
            }

            p.setColor(theme().text);
            UI.drawText(mode.displayName(), ox + RADIO_SIZE + ICON_TEXT_GAP, cy, Fonts.GoogleFlex.weight(550), 18f, Align.CENTER_LEFT, p);

            ox += optionWidth(mode) + OPTION_GAP;
        }
    }

    private void renderCycler(Paint p) {
        float bx = getX() + getWidth() - CYCLER_W;
        float by = getY() + (getHeight() - CYCLER_H) / 2f;

        p.setColor(theme().surfaceBright);
        UI.drawRRect(bx, by, CYCLER_W, CYCLER_H, 6f, p);
        p.setStroke(true);
        p.setStrokeWidth(1.5f);
        p.setColor(theme().text);
        UI.drawRRect(bx, by, CYCLER_W, CYCLER_H, 6f, p);
        p.setStroke(false);

        p.setColor(theme().text);
        UI.drawText(setting().getCurrentDisplayName(), bx + CYCLER_W / 2f, by + CYCLER_H / 2f, Fonts.GoogleFlex.weight(600), 18f, Align.CENTER, p);
        UI.drawText(MaterialIcons.ARROW_LEFT, bx + 6f, by + CYCLER_H / 2f, Fonts.MaterialIcons.weight(700).fill(true), 24f, Align.CENTER_LEFT, p);
        UI.drawText(MaterialIcons.ARROW_RIGHT, bx + CYCLER_W - 6f, by + CYCLER_H / 2f, Fonts.MaterialIcons.weight(700).fill(true), 24f, Align.CENTER_RIGHT, p);
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (event.type != EventInput.Type.MOUSE || event.action != EventInput.Action.PRESS || event.value != 0) return false;

        ModeSetting<T> setting = setting();

        if (compact()) {
            float bx = getX() + getWidth() - CYCLER_W;
            float by = getY() + (getHeight() - CYCLER_H) / 2f;
            if (mouseX >= bx && mouseX <= bx + CYCLER_W && mouseY >= by && mouseY <= by + CYCLER_H) {
                if (mouseX < bx + CYCLER_W / 2f) setting.prevMode();
                else setting.nextMode();
                return true;
            }
            return false;
        }

        float ox = getX() + getWidth() - optionsWidth();
        float cy = getY() + getHeight() / 2f;
        for (T mode : setting.getModes()) {
            float w = optionWidth(mode);
            if (mouseX >= ox && mouseX <= ox + w && mouseY >= cy - RADIO_SIZE / 2f && mouseY <= cy + RADIO_SIZE / 2f) {
                setting.set(mode);
                return true;
            }
            ox += w + OPTION_GAP;
        }
        return false;
    }
}
