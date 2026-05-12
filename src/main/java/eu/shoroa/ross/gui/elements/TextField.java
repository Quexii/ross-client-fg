package eu.shoroa.ross.gui.elements;

import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.gui.GuiElement;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.types.Size;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.PaintMode;
import org.lwjgl.input.Keyboard;

import java.util.function.Consumer;

public class TextField extends GuiElement {
    private static final int ACCENT = 0xFFe44c8a;
    private static final int COLOR_BG = 0xFF242424;
    private static final int COLOR_BG_ACTIVE = 0xFF2C2C2C;
    private static final int COLOR_BORDER = 0xFF444444;
    private static final int COLOR_TEXT = 0xFFE8E8E8;
    private static final int COLOR_PLACEHOLDER = 0xFF666666;
    private static final float RADIUS = 8f;
    private static final float H_PAD = 10f;
    private static final float TEXT_SIZE = 14f;
    private static final float ICON_SIZE = 15f;
    private static final float ICON_GAP = 7f;

    private final Animate focusAnim = new Animate(180L, Easing.CIRC_OUT);
    private final Animate hoverAnim = new Animate(120L, Easing.LINEAR);

    private final StringBuilder buffer = new StringBuilder();
    private boolean focused = false;
    private boolean hovered = false;
    private boolean cursorVisible = true;
    private long lastBlink = System.currentTimeMillis();

    private String placeholder = "";
    private Font iconFont = null;
    private String iconGlyph = null;
    private Runnable onEnter = null;
    private Consumer<String> onChanged = null;

    public TextField(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public TextField placeholder(String text) {
        this.placeholder = text;
        return this;
    }

    public TextField icon(Font font, String glyph) {
        this.iconFont = font;
        this.iconGlyph = glyph;
        return this;
    }

    public TextField onEnter(Runnable cb) {
        this.onEnter = cb;
        return this;
    }

    public TextField onChanged(Consumer<String> cb) {
        this.onChanged = cb;
        return this;
    }

    public String getText() {
        return buffer.toString();
    }

    public void setText(String v) {
        buffer.setLength(0);
        if (v != null) buffer.append(v);
    }

    public void clear() {
        buffer.setLength(0);
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean v) {
        this.focused = v;
    }

    @Override
    public void render(float mouseX, float mouseY, float partialTicks) {
        hovered = getBounds().contains(mouseX, mouseY);
        hoverAnim.doEase(hovered);
        focusAnim.doEase(focused);

        float fv = (float) focusAnim.getValue();
        float hv = (float) hoverAnim.getValue();

        long now = System.currentTimeMillis();
        if (now - lastBlink >= 530L) {
            cursorVisible = !cursorVisible;
            lastBlink = now;
        }

        float activity = Math.max(hv * 0.5f, fv);

        try (Paint p = new Paint()) {
            p.setColor(Color.makeLerp(COLOR_BG, COLOR_BG_ACTIVE, activity));
            Renderer.drawRRect(getX(), getY(), getWidth(), getHeight(), RADIUS, p);
        }

        try (Paint p = new Paint()) {
            p.setColor(Color.makeLerp(COLOR_BORDER, ACCENT, fv));
            p.setStrokeWidth(2f);
            p.setMode(PaintMode.STROKE);
            Renderer.drawRRect(getX(), getY(), getWidth(), getHeight(), RADIUS, p);
        }

        float contentX = getX() + H_PAD;
        if (iconFont != null && iconGlyph != null) {
            try (Paint p = new Paint()) {
                p.setColor(Color.makeLerp(COLOR_PLACEHOLDER, ACCENT, fv));
                Renderer.drawText(iconGlyph, contentX, getY() + getHeight() / 2f,
                        iconFont, ICON_SIZE, Font.Align.CENTER_LEFT, p);
            }
            Size iconSz = Renderer.getTextBounds(iconGlyph, iconFont, ICON_SIZE);
            contentX += iconSz.width + ICON_GAP;
        }

        float textEndX = getX() + getWidth() - H_PAD;
        float textY = getY() + getHeight() / 2f;
        float availW = textEndX - contentX;

        String visible = buffer.toString();
        if (!visible.isEmpty()) {
            Size sz = Renderer.getTextBounds(visible, Fonts.GoogleFlex.weight(400), TEXT_SIZE);
            while (sz.width > availW && visible.length() > 1) {
                visible = visible.substring(1);
                sz = Renderer.getTextBounds(visible, Fonts.GoogleFlex.weight(400), TEXT_SIZE);
            }
        }

        if (buffer.length() == 0 && !focused) {
            try (Paint p = new Paint()) {
                p.setColor(COLOR_PLACEHOLDER);
                Renderer.drawText(placeholder, contentX, textY,
                        Fonts.GoogleFlex.weight(600), TEXT_SIZE, Font.Align.CENTER_LEFT, p);
            }
        } else {
            try (Paint p = new Paint()) {
                p.setColor(COLOR_TEXT);
                Renderer.drawText(visible, contentX, textY,
                        Fonts.GoogleFlex.weight(500), TEXT_SIZE, Font.Align.CENTER_LEFT, p);
            }
        }

        if (focused && cursorVisible) {
            float cx = contentX
                    + (visible.isEmpty() ? 0f : Renderer.getTextBounds(visible, Fonts.GoogleFlex.weight(400), TEXT_SIZE).width)
                    + 1f;
            try (Paint p = new Paint()) {
                p.setColor(Color.makeLerp(COLOR_TEXT, ACCENT, fv));
                Renderer.drawRRect(cx, getY() + getHeight() * 0.25f, 2f, getHeight() * 0.5f, 2f, p);
            }
        }
    }

    @Override
    public boolean input(float mouseX, float mouseY, EventInput event) {
        if (event.type == EventInput.Type.MOUSE
                && event.action == EventInput.Action.PRESS
                && event.value == 0) {
            focused = getBounds().contains(mouseX, mouseY);
            return focused;
        }

        if (!focused) return false;

        if (event.type == EventInput.Type.KEYBOARD && event.action == EventInput.Action.PRESS) {
            switch (event.value) {
                case Keyboard.KEY_BACK:
                    if (buffer.length() > 0) {
                        buffer.deleteCharAt(buffer.length() - 1);
                        notify(buffer.toString());
                    }
                    return true;
                case Keyboard.KEY_RETURN:
                case Keyboard.KEY_NUMPADENTER:
                    if (onEnter != null) onEnter.run();
                    focused = false;
                    return true;
                case Keyboard.KEY_ESCAPE:
                    focused = false;
                    return true;
            }
        }
        if (event.type == EventInput.Type.CHARACTER && event.action == EventInput.Action.PRESS) {
            char c = (char) event.value;
            if (c >= 32) {
                buffer.append(c);
                notify(buffer.toString());
            }
            return true;
        }

        return false;
    }

    private void notify(String v) {
        if (onChanged != null) onChanged.accept(v);
    }
}