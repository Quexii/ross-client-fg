package eu.shoroa.ross.feature.gui.clickgui.stella.elements.settings;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.module.Bind;
import eu.shoroa.ross.feature.setting.BindSetting;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import io.github.humbleui.skija.Paint;
import org.lwjgl.input.Keyboard;

public class StellaBindSetting extends StellaSetting<Bind> {
    private static final float KEY_W = 190f;
    private static final float ACTION_W = 110f;
    private static final float CHIP_H = 38f;
    private static final float CHIP_GAP = 10f;

    private boolean listening = false;
    /** Action applied to the next captured key when nothing is bound yet. */
    private EventInput.Action pendingAction = EventInput.Action.PRESS;

    public StellaBindSetting(float x, float y, float width, float height, BindSetting setting) {
        super(x, y, width, height, setting);
    }

    private BindSetting setting() {
        return (BindSetting) getSetting();
    }

    private EventInput.Action action() {
        Bind bind = setting().get();
        return bind != null ? bind.action : pendingAction;
    }

    private float chipY() {
        return getY() + (getHeight() - CHIP_H) / 2f;
    }

    private float keyX() {
        return getX() + getWidth() - KEY_W;
    }

    private float actionX() {
        return keyX() - CHIP_GAP - ACTION_W;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        try (Paint p = new Paint()) {
            drawLabel(p);

            drawActionChip(p);
            drawKeyChip(p);
        }
    }

    private void drawActionChip(Paint p) {
        float x = actionX();
        float y = chipY();

        p.setColor(theme().surfaceBright);
        UI.drawRRect(x, y, ACTION_W, CHIP_H, 6f, p);
        p.setStroke(true);
        p.setStrokeWidth(1.5f);
        p.setColor(theme().foreground);
        UI.drawRRect(x, y, ACTION_W, CHIP_H, 6f, p);
        p.setStroke(false);

        p.setColor(theme().foreground);
        String label = action() == EventInput.Action.HOLD ? "Hold" : "Toggle";
        UI.drawText(label, x + ACTION_W / 2f, y + CHIP_H / 2f, Fonts.GoogleFlex.weight(600), 18f, Align.CENTER, p);
    }

    private void drawKeyChip(Paint p) {
        float x = keyX();
        float y = chipY();
        Bind bind = setting().get();

        if (listening) {
            p.setColor(theme().accent);
            UI.drawRRect(x, y, KEY_W, CHIP_H, 6f, p);
        } else {
            p.setColor(theme().surfaceBright);
            UI.drawRRect(x, y, KEY_W, CHIP_H, 6f, p);
        }
        p.setStroke(true);
        p.setStrokeWidth(1.5f);
        p.setColor(listening ? theme().outline : theme().foreground);
        UI.drawRRect(x, y, KEY_W, CHIP_H, 6f, p);
        p.setStroke(false);

        String label = listening ? "Press a key…" : bind != null ? bind.displayName() : "None";
        p.setColor(listening ? theme().foregroundContrast : bind != null ? theme().foreground : theme().foregroundMuted);
        UI.drawText(label, x + KEY_W / 2f, y + CHIP_H / 2f, Fonts.GoogleFlex.weight(600), 18f, Align.CENTER, p);
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (listening) {
            return capture(event);
        }

        if (event.type != EventInput.Type.MOUSE || event.action != EventInput.Action.PRESS || event.value != 0) {
            return false;
        }

        float y = chipY();
        if (mouseY < y || mouseY > y + CHIP_H) return false;

        if (mouseX >= keyX() && mouseX <= keyX() + KEY_W) {
            listening = true;
            return true;
        }

        if (mouseX >= actionX() && mouseX <= actionX() + ACTION_W) {
            EventInput.Action next = action() == EventInput.Action.HOLD
                    ? EventInput.Action.PRESS
                    : EventInput.Action.HOLD;
            Bind bind = setting().get();
            if (bind != null) {
                setting().set(new Bind(bind.key, bind.type, next));
            } else {
                pendingAction = next;
            }
            return true;
        }

        return false;
    }

    /** Consumes all input while listening so keys don't leak into the GUI. */
    private boolean capture(EventInput event) {
        switch (event.type) {
            case KEYBOARD:
                if (event.action == EventInput.Action.PRESS) {
                    if (event.value == Keyboard.KEY_ESCAPE) {
                        // keep the old bind
                    } else if (event.value == Keyboard.KEY_BACK || event.value == Keyboard.KEY_DELETE) {
                        setting().set(null);
                    } else {
                        setting().set(new Bind(event.value, EventInput.Type.KEYBOARD, action()));
                    }
                    listening = false;
                }
                return true;
            case CHARACTER:
                return true;
            case MOUSE:
                if (event.action == EventInput.Action.PRESS) {
                    // Side/middle buttons are bindable; left/right just cancel.
                    if (event.value >= 2) {
                        setting().set(new Bind(event.value, EventInput.Type.MOUSE, action()));
                    }
                    listening = false;
                }
                return true;
            default:
                return true;
        }
    }
}
