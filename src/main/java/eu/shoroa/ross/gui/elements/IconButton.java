package eu.shoroa.ross.gui.elements;

import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;

public class IconButton extends ButtonBase {
    private final Font iconFont;
    private final String icon;

    public IconButton(float x, float y, float width, float height, Font iconFont, String icon) {
        super(x, y, width, height);
        this.iconFont = iconFont;
        this.icon = icon;

        hoverAnimation.setDuration(150L);
        pressAnimation.setDuration(100L);
    }

    @Override
    protected void renderButton(float mouseX, float mouseY, float partialTicks) {
        final float radius = 14f;

        final int accent = 0xFFe44c8a;

        int text = Color.makeLerp(0xFF000000, 0xFFDFDFDF, (float) hoverAnimation.getValue());
        int backgroundHover = Color.makeLerp(0xFFDADADA, 0xFF323232, (float) hoverAnimation.getValue());
        int strokeIn = Color.makeLerp(0xFFEFEFEF, 0xFF212121, (float) hoverAnimation.getValue());
        int strokeOut = Color.makeLerp(0xFF323232, 0xFFEFEFEF, (float) hoverAnimation.getValue());

        int background = Color.makeLerp(backgroundHover, accent, (float) pressAnimation.getValue());

        try (Paint p = new Paint()) {
            p.setColor(strokeOut);
            Renderer.drawRRect(getX(), getY(), getWidth(), getHeight(), radius, p);

            float offset = (float) (2f - hoverAnimation.getValue() * 0.5);

            p.setColor(strokeIn);
            Renderer.drawRRect(getX() + offset, getY() + offset, getWidth() - offset * 2, getHeight() - offset * 2, radius - offset, p);

            offset = 4f;
            p.setColor(background);
            Renderer.drawRRect(getX() + offset, getY() + offset, getWidth() - offset * 2, getHeight() - offset * 2, radius - offset, p);

            p.setColor(text);
            Renderer.drawText(icon, getX() + getWidth() / 2f, getY() + getHeight() / 2f, iconFont, 30f, Font.Align.CENTER, p);
        }
    }
}
