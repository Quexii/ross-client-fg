package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.HUDAnchor;
import eu.shoroa.ross.feature.module.HUDElement;
import eu.shoroa.ross.feature.module.HUDModule;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.Size;
import io.github.humbleui.skija.Paint;
import net.minecraft.util.MathHelper;

import static eu.shoroa.ross.Client.mc;

public class ModuleCoordinates extends HUDModule {
    private static final String[] DIRECTIONS = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};

    private static final float HEIGHT = 46f;
    private static final float PAD = 6f;
    private static final float CHIP = HEIGHT - PAD * 2f;
    private static final float COLUMN_WIDTH = 78f;
    private static final float LABEL_SIZE = 13f;
    private static final float VALUE_SIZE = 18f;

    private final SettingCategory settings = addCategory("Settings", ".", "settings");
    private final BooleanSetting showDirection = register(new BooleanSetting("Show direction", "direction", true), settings);

    public ModuleCoordinates() {
        super("Coordinates", "Shows your position and facing", MaterialIcons.EXPLORE);
        addElement(new Element());
    }

    private class Element extends HUDElement {
        protected Element() {
            super("main");
            setPlacement(HUDAnchor.LEFT_BOTTOM, 10, -10);
        }

        @Override
        public void render(Hud.Layer layer) {
            if (!layer.is(Hud.Layer.NAME_SKIA_BOTTOM)) return;
            if (mc.thePlayer == null && !isInEditor()) return;

            StellaTheme t = StellaTheme.get();
            float x = getBounds().x;
            float y = getBounds().y;

            int px = 0, py = 0, pz = 0;
            String direction = "N";
            if (mc.thePlayer != null) {
                px = MathHelper.floor_double(mc.thePlayer.posX);
                py = MathHelper.floor_double(mc.thePlayer.posY);
                pz = MathHelper.floor_double(mc.thePlayer.posZ);
                direction = DIRECTIONS[MathHelper.floor_double(mc.thePlayer.rotationYaw * 8.0 / 360.0 + 0.5) & 7];
            }

            try (Paint p = new Paint()) {
                StellaHud.card(x, y, getSize().width, HEIGHT, p);
                StellaHud.iconChip(x + PAD, y + PAD, CHIP, MaterialIcons.EXPLORE, p);

                float columnX = x + PAD + CHIP + 11f;
                drawAxis(p, "X", Integer.toString(px), columnX, y);
                drawAxis(p, "Y", Integer.toString(py), columnX + COLUMN_WIDTH, y);
                drawAxis(p, "Z", Integer.toString(pz), columnX + COLUMN_WIDTH * 2f, y);

                if (showDirection.get()) {
                    float chipX = columnX + COLUMN_WIDTH * 3f + 2f;
                    p.setColor(t.surfaceDim);
                    UI.drawRRect(chipX, y + PAD + 2f, 34f, CHIP - 4f, StellaHud.RADIUS * 0.7f, p);
                    p.setStroke(true);
                    p.setStrokeWidth(1.5f);
                    p.setColor(t.border);
                    UI.drawRRect(chipX, y + PAD + 2f, 34f, CHIP - 4f, StellaHud.RADIUS * 0.7f, p);
                    p.setStroke(false);

                    p.setColor(t.accentDeep);
                    UI.drawText(direction, chipX + 17f, y + HEIGHT / 2f, Fonts.GoogleFlex.weight(700).roundness(25), 15f, Align.CENTER, p);
                }
            }
        }

        private void drawAxis(Paint p, String axis, String value, float x, float y) {
            StellaTheme t = StellaTheme.get();

            p.setColor(t.accent);
            UI.drawText(axis, x, y + HEIGHT / 2f, Fonts.GoogleFlex.weight(700).roundness(25), LABEL_SIZE, Align.CENTER_LEFT, p);

            p.setColor(t.foreground);
            UI.drawText(value, x + 13f, y + HEIGHT / 2f, Fonts.GoogleFlex.weight(600).roundness(25), VALUE_SIZE, Align.CENTER_LEFT, p);
        }

        @Override
        public void dummy(Hud.Layer layer) {
            render(layer);
        }

        @Override
        public Size getSize() {
            float width = PAD + CHIP + 11f + COLUMN_WIDTH * 3f;
            if (showDirection.get()) width += 2f + 34f;
            return new Size(width + 10f, HEIGHT);
        }
    }
}
