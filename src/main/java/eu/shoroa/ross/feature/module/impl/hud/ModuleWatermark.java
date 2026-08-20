package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.HUDAnchor;
import eu.shoroa.ross.feature.module.HUDElement;
import eu.shoroa.ross.feature.module.HUDModule;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.fml.RossMod;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.Size;
import io.github.humbleui.skija.Paint;

import static eu.shoroa.ross.Client.mc;

public class ModuleWatermark extends HUDModule {
    private static final float HEIGHT = 46f;
    private static final float PAD = 6f;
    private static final float CHIP = HEIGHT - PAD * 2f;
    private static final float NAME_SIZE = 21f;
    private static final float SUB_SIZE = 12f;

    private final SettingCategory settings = addCategory("Settings", ".", "settings");
    private final BooleanSetting showVersion = register(new BooleanSetting("Show version", "version", true), settings);
    private final BooleanSetting showUser = register(new BooleanSetting("Show username", "user", false), settings);

    public ModuleWatermark() {
        super("Watermark", "Shows the client branding", MaterialIcons.DIAMOND);
    }

    private class Element extends HUDElement {
        protected Element() {
            super("main");
            setPlacement(HUDAnchor.LEFT_TOP, 10, 10);
        }

        @Override
        public void render(Hud.Layer layer) {
            if (!layer.is(Hud.Layer.NAME_SKIA_BOTTOM)) return;

            StellaTheme t = StellaTheme.get();
            float x = getBounds().x;
            float y = getBounds().y;
            String subline = subline();

            try (Paint p = new Paint()) {
                StellaHud.card(x, y, getSize().width, HEIGHT, p);
                StellaHud.iconChip(x + PAD, y + PAD, CHIP, MaterialIcons.DIAMOND, p);

                float textX = x + PAD + CHIP + 9f;
                p.setColor(t.foreground);
                if (subline.isEmpty()) {
                    UI.drawText("Ross", textX, y + HEIGHT / 2f, Fonts.GoogleFlex.weight(700).roundness(25), NAME_SIZE, Align.CENTER_LEFT, p);
                } else {
                    UI.drawText("Ross", textX, y + HEIGHT / 2f - 6.5f, Fonts.GoogleFlex.weight(700).roundness(25), NAME_SIZE, Align.CENTER_LEFT, p);
                    p.setColor(t.foregroundContrast);
                    UI.drawText(subline, textX, y + HEIGHT / 2f + 11f, Fonts.GoogleFlex.weight(500), SUB_SIZE, Align.CENTER_LEFT, p);
                }
            }
        }

        @Override
        public void dummy(Hud.Layer layer) {
            render(layer);
        }

        @Override
        public Size getSize() {
            float textWidth = UI.getTextBounds("Ross", Fonts.GoogleFlex.weight(700).roundness(25), NAME_SIZE).width;
            String subline = subline();
            if (!subline.isEmpty()) {
                textWidth = Math.max(textWidth, UI.getTextBounds(subline, Fonts.GoogleFlex.weight(500), SUB_SIZE).width);
            }
            return new Size(PAD + CHIP + 9f + textWidth + 12f, HEIGHT);
        }

        private String subline() {
            StringBuilder sb = new StringBuilder();
            if (showVersion.get()) sb.append("v").append(RossMod.VERSION);
            if (showUser.get() && mc.getSession() != null) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(mc.getSession().getUsername());
            }
            return sb.toString();
        }
    }
}
