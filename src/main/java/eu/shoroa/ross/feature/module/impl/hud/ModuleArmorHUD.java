package eu.shoroa.ross.feature.module.impl.hud;

import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.HUDAnchor;
import eu.shoroa.ross.feature.module.HUDElement;
import eu.shoroa.ross.feature.module.HUDModule;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.ModeEnum;
import eu.shoroa.ross.feature.setting.ModeSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.opengl.Renderer2D;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.type.Size;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import static eu.shoroa.ross.Client.mc;

public class ModuleArmorHUD extends HUDModule {
    private static final float PAD = 6f;
    private static final float CELL = 40f;
    private static final float GAP = 5f;
    private static final float ITEM = 32f;

    public enum Mode implements ModeEnum {
        HORIZONTAL, VERTICAL;

        @Override
        public String displayName() {
            return this == HORIZONTAL ? "Horizontal" : "Vertical";
        }
    }

    private final SettingCategory settings = addCategory("Settings", ".", "settings");
    private final ModeSetting<Mode> mode = register(new ModeSetting<>("Layout", "layout", Mode.HORIZONTAL), settings);
    private final BooleanSetting showHeld = register(new BooleanSetting("Show held item", "held", true), settings);

    public ModuleArmorHUD() {
        super("Armor HUD", "Shows your equipped armor and held item", MaterialIcons.SHIELD);
        addElement(new Element());
    }

    private class Element extends HUDElement {
        protected Element() {
            super("main");
            setPlacement(HUDAnchor.CENTER_BOTTOM, 0, -64);
        }

        @Override
        public void render(Hud.Layer layer) {
            if (mc.thePlayer == null) return;

            if (layer.is(Hud.Layer.NAME_SKIA_BOTTOM)) {
                renderCard();
            } else if (layer.is(Hud.Layer.NAME_VANILLA_BOTTOM)) {
                renderItems();
            }
        }

        private void renderCard() {
            StellaTheme t = StellaTheme.get();
            float x = getBounds().x;
            float y = getBounds().y;
            int slots = slotCount();

            try (Paint p = new Paint()) {
                StellaHud.card(x, y, getSize().width, getSize().height, p);

                for (int i = 0; i < slots; i++) {
                    float cellX = x + cellOffsetX(i);
                    float cellY = y + cellOffsetY(i);
                    p.setColor(t.surfaceDim);
                    UI.drawRRect(cellX, cellY, CELL, CELL, StellaHud.RADIUS * 0.7f, p);
                    p.setStroke(true);
                    p.setStrokeWidth(1.5f);
                    p.setColor(t.border);
                    UI.drawRRect(cellX, cellY, CELL, CELL, StellaHud.RADIUS * 0.7f, p);
                    p.setStroke(false);
                }
            }
        }

        private void renderItems() {
            float x = getBounds().x;
            float y = getBounds().y;
            int scale = new ScaledResolution(mc).getScaleFactor();
            float inset = (CELL - ITEM) / 2f;
            ItemStack[] stacks = collectStacks();

            GlStateManager.pushMatrix();
            GlStateManager.scale(1f / scale, 1f / scale, 1f);
            for (int i = 0; i < stacks.length; i++) {
                if (stacks[i] == null) continue;

                GlStateManager.pushMatrix();
                GlStateManager.translate(x + cellOffsetX(i) + inset, y + cellOffsetY(i) + inset, 0f);
                GlStateManager.scale(ITEM / 16f, ITEM / 16f, 1f);
                Renderer2D.drawItem(stacks[i], 0f, 0f);
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
        }

        private ItemStack[] collectStacks() {
            int slots = slotCount();
            ItemStack[] stacks = new ItemStack[slots];
            // Helmet first, boots last.
            for (int i = 0; i < 4; i++) {
                stacks[i] = mc.thePlayer.inventory.armorInventory[3 - i];
            }
            if (showHeld.get()) {
                stacks[4] = mc.thePlayer.getHeldItem();
            }
            return stacks;
        }

        private int slotCount() {
            return showHeld.get() ? 5 : 4;
        }

        private float cellOffsetX(int index) {
            return mode.getCurrent() == Mode.HORIZONTAL ? PAD + index * (CELL + GAP) : PAD;
        }

        private float cellOffsetY(int index) {
            return mode.getCurrent() == Mode.VERTICAL ? PAD + index * (CELL + GAP) : PAD;
        }

        @Override
        public void dummy(Hud.Layer layer) {
            render(layer);
        }

        @Override
        public Size getSize() {
            int slots = slotCount();
            float longSide = PAD * 2f + slots * CELL + (slots - 1) * GAP;
            float shortSide = PAD * 2f + CELL;
            return mode.getCurrent() == Mode.HORIZONTAL
                    ? new Size(longSide, shortSide)
                    : new Size(shortSide, longSide);
        }
    }
}
