package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.HUDAnchor;
import eu.shoroa.ross.feature.module.HUDElement;
import eu.shoroa.ross.feature.module.HUDModule;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.Size;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.shoroa.ross.Client.mc;

public class ModulePotionEffects extends HUDModule {
    private static final String[] LEVELS = {"", " II", " III", " IV", " V", " VI", " VII", " VIII", " IX", " X"};

    private static final float WIDTH = 190f;
    private static final float PAD = 10f;
    private static final float ROW_HEIGHT = 36f;
    private static final float BAR_HEIGHT = 4f;

    private final Map<Integer, Integer> maxDurations = new HashMap<>();

    public ModulePotionEffects() {
        super("Potion Effects", "Shows your active potion effects", MaterialIcons.SCIENCE);
        addElement(new Element());
    }

    private class Element extends HUDElement {
        public Element() {
            super("main");
            setPlacement(HUDAnchor.RIGHT_TOP, -10, 66);
        }

        @Override
        public void render(Hud.Layer layer) {
            if (!layer.is(Hud.Layer.NAME_SKIA_BOTTOM)) return;

            List<Row> rows = buildRows();
            if (rows.isEmpty()) return;

            float x = getBounds().x;
            float y = getBounds().y;
            StellaTheme t = StellaTheme.get();

            try (Paint p = new Paint()) {
                StellaHud.card(x, y, WIDTH, getSize().height, p);

                float rowY = y + PAD;
                for (Row row : rows) {
                    p.setColor(t.foreground);
                    UI.drawText(row.name, x + PAD, rowY + 10f, Fonts.GoogleFlex.weight(600).roundness(25), 15f, Align.CENTER_LEFT, p);

                    p.setColor(t.foregroundMuted);
                    UI.drawText(row.time, x + WIDTH - PAD, rowY + 10f, Fonts.GoogleFlex.weight(500), 13f, Align.CENTER_RIGHT, p);

                    float barWidth = WIDTH - PAD * 2f;
                    p.setColor(t.track);
                    UI.drawRRect(x + PAD, rowY + 22f, barWidth, BAR_HEIGHT, BAR_HEIGHT / 2f, p);
                    p.setColor(t.accent);
                    UI.drawRRect(x + PAD, rowY + 22f, barWidth * row.fraction, BAR_HEIGHT, BAR_HEIGHT / 2f, p);

                    rowY += ROW_HEIGHT;
                }
            }
        }

        @Override
        public void dummy(Hud.Layer layer) {
            render(layer);
        }

        @Override
        public Size getSize() {
            int count = Math.max(1, buildRows().size());
            return new Size(WIDTH, PAD * 2f + count * ROW_HEIGHT - 8f);
        }

        private List<Row> buildRows() {
            List<Row> rows = new ArrayList<>();

            if (mc.thePlayer != null && !mc.thePlayer.getActivePotionEffects().isEmpty()) {
                for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
                    Potion potion = Potion.potionTypes[effect.getPotionID()];
                    if (potion == null) continue;

                    int max = maxDurations.merge(effect.getPotionID(), effect.getDuration(), Math::max);
                    String name = I18n.format(potion.getName());
                    if (effect.getAmplifier() > 0 && effect.getAmplifier() < LEVELS.length) {
                        name += LEVELS[effect.getAmplifier()];
                    }

                    rows.add(new Row(name, Potion.getDurationString(effect), (float) effect.getDuration() / max));
                }
            } else {
                maxDurations.clear();
                if (isInEditor()) {
                    rows.add(new Row("Speed II", "1:24", 0.72f));
                    rows.add(new Row("Regeneration", "0:38", 0.35f));
                }
            }

            return rows;
        }
    }

    private static final class Row {
        final String name;
        final String time;
        final float fraction;

        Row(String name, String time, float fraction) {
            this.name = name;
            this.time = time;
            this.fraction = fraction;
        }
    }
}
