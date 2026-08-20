package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.HUDElement;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.Size;
import io.github.humbleui.skija.Paint;

public abstract class TextHUDElement extends HUDElement {
    protected static final float HEIGHT = 42f;

    private static final float PAD = 6f;
    private static final float CHIP = HEIGHT - PAD * 2f;
    private static final float VALUE_SIZE = 20f;
    private static final float SUFFIX_SIZE = 13f;

    private final String chipIcon;

    protected TextHUDElement(String id, String chipIcon) {
        super(id);
        this.chipIcon = chipIcon;
    }

    protected TextHUDElement(String id, MaterialIcons chipIcon) {
        this(id, chipIcon.toString());
    }

    protected abstract String value();

    protected abstract String suffix();

    @Override
    public void render(Hud.Layer layer) {
        if (!layer.is(Hud.Layer.NAME_SKIA_BOTTOM)) {
            return;
        }

        StellaTheme t = StellaTheme.get();

        float x = getBounds().x;
        float y = getBounds().y;

        String value = value();
        String suffix = suffix();

        try (Paint p = new Paint()) {
            StellaHud.card(x, y, getSize().width, HEIGHT, p);

            StellaHud.iconChip(x + PAD, y + PAD, CHIP, chipIcon, p);

            float textX = x + PAD + CHIP + 9f;

            p.setColor(t.foreground);

            UI.drawText(value, textX, y + HEIGHT / 2f, Fonts.GoogleFlex.weight(650).roundness(25), VALUE_SIZE, Align.CENTER_LEFT, p);

            if (!suffix.isEmpty()) {
                float valueWidth = UI.getTextBounds(value, Fonts.GoogleFlex.weight(650).roundness(25), VALUE_SIZE).width;

                p.setColor(t.foregroundContrast);

                UI.drawText(suffix, textX + valueWidth + 5f, y + HEIGHT / 2f + 2.5f, Fonts.GoogleFlex.weight(500), SUFFIX_SIZE, Align.CENTER_LEFT, p);
            }
        }
    }

    @Override
    public Size getSize() {
        String value = value();
        String suffix = suffix();

        float width = PAD + CHIP + 9f + UI.getTextBounds(value, Fonts.GoogleFlex.weight(650).roundness(25), VALUE_SIZE).width;

        if (!suffix.isEmpty()) {
            width += 5f + UI.getTextBounds(suffix, Fonts.GoogleFlex.weight(500), SUFFIX_SIZE).width;
        }

        return new Size(width + 11f, HEIGHT);
    }
}
