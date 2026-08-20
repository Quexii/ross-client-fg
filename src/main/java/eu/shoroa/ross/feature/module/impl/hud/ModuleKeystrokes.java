package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.HUDAnchor;
import eu.shoroa.ross.feature.module.HUDElement;
import eu.shoroa.ross.feature.module.HUDModule;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.animate.Animate;
import eu.shoroa.ross.render.animate.Easing;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.DampFloat;
import eu.shoroa.ross.type.Size;
import eu.shoroa.ross.utils.math.Mth;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.FilterBlurMode;
import io.github.humbleui.skija.MaskFilter;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import static eu.shoroa.ross.Client.mc;

public class ModuleKeystrokes extends HUDModule {

    private final SettingCategory categoryComponents = addCategory("Components", ".", "components");
    private final BooleanSetting showJump = register(new BooleanSetting("Show Jump", "showJump", true), categoryComponents);
    private final BooleanSetting showStrokes = register(new BooleanSetting("Show Strokes", "showStrokes", false), categoryComponents);

    private final DampFloat animatedHeight = new DampFloat();
    private final DampFloat opacityJump = new DampFloat();
    private final DampFloat opacityStrokes = new DampFloat();

    private final float CELL_SIZE = 50;
    private final float CELL_GAP = 5;

    private final float STROKE_HEIGHT = 70f;

    public ModuleKeystrokes() {
        super("Keystrokes", ".", MaterialIcons.KEYBOARD);
        addElement(new Element());
    }

    private class Element extends HUDElement {
        private final Key KEY_FORWARD = new Key(mc.gameSettings.keyBindForward, 1, 0, 1, 1);
        private final Key KEY_BACKWARD = new Key(mc.gameSettings.keyBindBack, 1, 1, 1, 1);
        private final Key KEY_LEFT = new Key(mc.gameSettings.keyBindLeft, 0, 1, 1, 1);
        private final Key KEY_RIGHT = new Key(mc.gameSettings.keyBindRight, 2, 1, 1, 1);
        private final Key KEY_JUMP = new Key(mc.gameSettings.keyBindJump, 0, 2, 3, 1);

        protected Element() {
            super("main");
            setPlacement(HUDAnchor.LEFT_TOP, 10, 10);
        }

        @Override
        public void render(Hud.Layer layer) {
            if (layer.is(Hud.Layer.NAME_SKIA_BOTTOM)) {
                float requiredHeight = 2 * CELL_SIZE + CELL_GAP;

                if (showJump.get()) {
                    requiredHeight += CELL_SIZE + CELL_GAP;
                }

                if (showStrokes.get()) {
                    requiredHeight += STROKE_HEIGHT + CELL_GAP;
                }

                Mth.smoothDamp(animatedHeight, requiredHeight, 0.1f, (float) Animate.getDelta());
                Mth.smoothDamp(opacityJump, showJump.get() ? 1.0f : 0.0f, 0.1f, (float) Animate.getDelta());
                Mth.smoothDamp(opacityStrokes, showStrokes.get() ? 1.0f : 0.0f, 0.1f, (float) Animate.getDelta());

                KEY_FORWARD.render();
                KEY_BACKWARD.render();
                KEY_LEFT.render();
                KEY_RIGHT.render();

                UI.saveLayerAlpha(getBounds().x - 80, getBounds().y - 80, getBounds().width + 160, getBounds().height + 160, opacityJump.value);
                UI.translate(0f, -10 * (1 - opacityJump.value));
                KEY_JUMP.render();
                UI.restore();

                if (showStrokes.get()) {

                }
            }
        }

        @Override
        public void dummy(Hud.Layer layer) {
            render(layer);
        }

        @Override
        public Size getSize() {
            return new Size(3 * CELL_SIZE + 2 * CELL_GAP, animatedHeight.value);
        }

        private class Key {
            private final KeyBinding key;
            private final int cellOffsetX;
            private final int cellOffsetY;
            private final int cellSizeX;
            private final int cellSizeY;

            private final Animate pressAnim = new Animate(150L, Easing.CIRC_OUT);

            private Key(KeyBinding key, int cellOffsetX, int cellOffsetY, int cellSizeX, int cellSizeY) {
                this.key = key;
                this.cellOffsetX = cellOffsetX;
                this.cellOffsetY = cellOffsetY;
                this.cellSizeX = cellSizeX;
                this.cellSizeY = cellSizeY;
            }

            public void render() {
                pressAnim.doEase(key.isKeyDown());

                StellaTheme t = StellaTheme.get();

                String keyName = key.getKeyCode() < 0 ? Mouse.getButtonName(key.getKeyCode()) : Keyboard.getKeyName(key.getKeyCode());

                float x = (getBounds().x + cellOffsetX * (CELL_SIZE + CELL_GAP));
                float y = (getBounds().y + cellOffsetY * (CELL_SIZE + CELL_GAP));
                float width = cellSizeX * CELL_SIZE + (cellSizeX - 1) * CELL_GAP;
                float height = cellSizeY * CELL_SIZE + (cellSizeY - 1) * CELL_GAP;

                int textColor = Color.makeLerp(t.foreground, t.surface, (float) pressAnim.getLinearValue());
                int bgColor = Color.makeLerp(t.surface, t.accent, (float) pressAnim.getLinearValue());

                try (Paint p = new Paint()) {
                    p.setColor(t.shadow);
                    p.setMaskFilter(MaskFilter.makeBlur(FilterBlurMode.NORMAL, 5f));
                    UI.drawRRect(x, y + 6f, width, height, 8f, p);
                    p.setMaskFilter(null);

                    p.setColor(t.surface);
                    UI.drawRRect(x, y, width, height, 8, p);

                    float pressW = (float) (width * pressAnim.getValue());
                    float pressH = (float) (height * pressAnim.getValue());

                    p.setColor(bgColor);
                    UI.drawRRect(x + (width - pressW) / 2f, y + (height - pressH) / 2f, pressW, pressH, 8, p);

                    p.setStroke(true);
                    p.setStrokeWidth(1.5f);
                    p.setColor(t.outline);
                    UI.drawRRect(x, y, width, height, 8, p);

                    p.setStroke(false);
                    p.setColor(textColor);
                    UI.drawText(keyName, x + width / 2f, y + height / 2f, Fonts.GoogleFlex.weight(600).roundness(25), 20f, Align.CENTER, p);
                }
            }
        }
    }
}
