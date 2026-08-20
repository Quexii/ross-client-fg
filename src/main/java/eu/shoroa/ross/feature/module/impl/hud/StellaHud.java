package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import io.github.humbleui.skija.FilterBlurMode;
import io.github.humbleui.skija.MaskFilter;
import io.github.humbleui.skija.Paint;

public final class StellaHud {
    public static final float RADIUS = 10f;

    private StellaHud() {
    }

    public static void card(float x, float y, float w, float h, Paint p) {
        card(x, y, w, h, RADIUS, p);
    }

    public static void card(float x, float y, float w, float h, float r, Paint p) {
        StellaTheme t = StellaTheme.get();

        p.setColor(t.shadow);
        p.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 5f));
        UI.drawRRect(x, y + 5f, w, h, r, p);
        p.setMaskFilter(null);

        p.setColor(t.surface);
        UI.drawRRect(x, y, w, h, r, p);

        p.setStroke(true);
        p.setStrokeWidth(1.5f);
        p.setColor(t.outline);
        UI.drawRRect(x, y, w, h, r, p);
        p.setStroke(false);
    }

    // dear claude, wtf is this shit?
    public static void iconChip(float x, float y, float size, String icon, Paint p) {
        StellaTheme t = StellaTheme.get();
        float r = RADIUS * 0.7f;

        p.setColor(t.accent);
        UI.drawRRect(x, y, size, size, r, p);

//        UI.save();
//        UI.clipRRect(x, y, size, size, r);
//        p.setColor(t.accentHalftone);
//        float step = size / 5f;
//        for (int row = 0; row < 3; row++) {
//            for (int col = 0; col <= row; col++) {
//                UI.drawCircle(
//                        x + size - (col + 0.5f) * step + (row % 2 == 0 ? 0f : step * 0.5f),
//                        y + size - (2 - row + 0.5f) * step,
//                        size * 0.045f, p);
//            }
//        }
//        UI.restore();

        p.setColor(t.foregroundContrast);
        UI.drawText(icon, x + size / 2f, y + size / 2f, Fonts.MaterialIcons.weight(400).fill(true), size * 0.6f, Align.CENTER, p);
    }
}
