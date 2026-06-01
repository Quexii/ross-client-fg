package eu.shoroa.ross.module.impl.hud;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.EventInGameHUD;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.ui.api.*;
import eu.shoroa.ross.util.MathHelper;
import eu.shoroa.ross.util.render.Renderer2D;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.yoga.Yoga;

import java.util.HashMap;
import java.util.Map;

import static eu.shoroa.ross.Client.mc;

public class ModuleModernHUD extends Module {
    private float smoothHealth = 0f;
    private float smoothAbsorption = 0f;
    private float smoothXP = 0f;

    private final Node rootNode = new Node();
    private final Node healthNode = new Node();
    private final Node xpNode = new Node();
    private final Node hotbarNode = new Node();
    private final Node[] slotNodes = new Node[9];
    private final Map<Integer, HotbarSlot> slots = new HashMap<>();

    public ModuleModernHUD() {
        super("ModernHUD", "Replaces the hotbar and health HUD with an anime Gatcha styled one.", Category.HUD);

        rootNode.width(Display.getWidth());
        rootNode.height(Display.getHeight());
        rootNode.direction(Direction.COLUMN_REVERSE);
        rootNode.justify(Justify.FLEX_START);
        rootNode.alignItems(Align.CENTER);

        final float healthWidth = 320f;
        final float healthHeight = 20f;
        healthNode.width(healthWidth);
        healthNode.height(healthHeight);
        healthNode.margin(Edge.BOTTOM, 22f);
        healthNode.direction(Direction.ROW);
        healthNode.justify(Justify.FLEX_START);
        healthNode.alignItems(Align.CENTER);
        healthNode.overflow(Overflow.VISIBLE);

        final float xpSize = 32f;
        final float xpOffset = (xpSize - healthHeight) / 2f;
        xpNode.width(xpSize);
        xpNode.height(xpSize);
        xpNode.position(PositionType.ABSOLUTE);
        xpNode.left(-xpOffset);
        xpNode.top(-xpOffset);

        healthNode.children(xpNode);
        rootNode.children(healthNode);

        final float slotSize = 42;
        hotbarNode.direction(Direction.ROW);
        hotbarNode.gap(Gutter.COLUMN, 6f);
        hotbarNode.margin(Edge.BOTTOM, 18f);
        for (int i = 0; i < 9; i++) {
            slotNodes[i] = new Node();
            slotNodes[i].width(slotSize);
            slotNodes[i].height(slotSize);
            hotbarNode.children(slotNodes[i]);

            slots.put(i, new HotbarSlot());
        }

        rootNode.children(hotbarNode);
    }

    @Subscribe
    public void oe$HideVanillaStats(EventInGameHUD.Stats event) {
        event.setCanceled(true);
    }

    @Subscribe
    public void oe$HideVanillaHotbar(EventInGameHUD.Hotbar event) {
        event.setCanceled(true);
    }

    @Subscribe
    public void oe$HideVanillaXP(EventInGameHUD.XP event) {
        event.setCanceled(true);
    }

    @Subscribe
    public void oe$BottomSkia(EventHUD.BottomSkia event) {
        if (rootNode.getWidth() != Display.getWidth() || rootNode.getHeight() != Display.getHeight()) {
            rootNode.width(Display.getWidth());
            rootNode.height(Display.getHeight());
            rootNode.markDirty();
        }

        if (rootNode.consumeDirty()) {
            rootNode.calcLayout(Yoga.YGUndefined, Yoga.YGUndefined, LayoutDirection.LTR);
            rootNode.resolveAbsolutePositions(0, 0);
        }

        final int fps = Math.max(Minecraft.getDebugFPS(), 1);
        final float lerpDelta = (1f / fps) * 8f;
        final Canvas canvas = Client.INSTANCE.skia.getCanvas();

        { // health bar
            float health = mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth();
            float absorption = mc.thePlayer.getAbsorptionAmount() / mc.thePlayer.getMaxHealth();

            smoothHealth = MathHelper.lerp(smoothHealth, health, lerpDelta);
            smoothAbsorption = MathHelper.lerp(smoothAbsorption, absorption, lerpDelta);

            if (Float.isNaN(smoothHealth) || Float.isInfinite(smoothHealth) || smoothHealth <= 0) {
                smoothHealth = health;
            }

            if (Float.isNaN(smoothAbsorption) || Float.isInfinite(smoothAbsorption) || smoothAbsorption <= 0) {
                smoothAbsorption = absorption;
            }

            try (Paint p = new Paint()) {
                p.setColor(0xFF282828);
                Renderer.drawRRect(healthNode.getX(), healthNode.getY(), healthNode.getWidth(), healthNode.getHeight(), 10f, p);
            }
            float stroke = 2f;

            try (Paint p = new Paint()) {
                p.setColor(0xFF343434);
                Renderer.drawRRect(healthNode.getX() + stroke, healthNode.getY() + stroke, healthNode.getWidth() - stroke * 2, healthNode.getHeight() - stroke * 2, 10f, p);
            }

            Renderer.save();
            Renderer.clipRRect(healthNode.getX() + stroke, healthNode.getY() + stroke, healthNode.getWidth() - stroke * 2, healthNode.getHeight() - stroke * 2, 10f);
            float smoothHealthW = (healthNode.getWidth() - stroke * 2) * smoothHealth;
            try (Paint p = new Paint()) {
                p.setColor(0xFF5afad1);
                if (smoothHealthW > 0) {
                    Renderer.drawRect(healthNode.getX() + stroke, healthNode.getY() + stroke, smoothHealthW, healthNode.getHeight() - stroke * 2, p);
                }
            }
            Renderer.restore();

            final float smoothAbsorptionW = healthNode.getWidth() * smoothAbsorption;
            try (Paint p = new Paint()) {
                p.setColor(0xFF282828);
                if (smoothAbsorptionW > 0) {
                    Renderer.drawRRect(healthNode.getX() + 3, healthNode.getY(), smoothAbsorptionW, healthNode.getHeight(), 10f, p);
                }
                p.setColor(0xFFfcfa8a);
                Renderer.drawRRect(healthNode.getX() + stroke, healthNode.getY() + stroke, (healthNode.getWidth() - stroke * 2) * smoothAbsorption, healthNode.getHeight() - stroke * 2, 10f, p);
            }

            Font font = Fonts.Geist.weight(600);

            String label = (int) ((mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) / 20f * 100f) + " / " + (int) (mc.thePlayer.getMaxHealth() / 20f * 100f);

            boolean isVulnerable = false;

            NetHandlerPlayClient client = mc.getNetHandler();
            if (client != null) {
                NetworkPlayerInfo npi = client.getPlayerInfo(mc.thePlayer.getUniqueID());
                if (npi != null) {
                    if (npi.getGameType().isSurvivalOrAdventure()) {
                        isVulnerable = true;
                    }
                }
            }
            if (!isVulnerable) {
                label = "Invulnerable";
            }

            try (Paint p = new Paint()) {
                p.setImageFilter(ImageFilter.makeDropShadow(0f, 0f, 3f, 3f, 0xAA000000));
                p.setStroke(true);
                p.setStrokeWidth(2f);
                p.setColor(0xFF000000);
                Renderer.drawText(label, healthNode.getX() + healthNode.getWidth() / 2f, healthNode.getY() + healthNode.getHeight() / 2f + 10 - 10, font, 14f, Font.Align.CENTER, p);
                p.setColor(-1);
                p.setStroke(false);
                p.setImageFilter(null);
                Renderer.drawText(label, healthNode.getX() + healthNode.getWidth() / 2f, healthNode.getY() + healthNode.getHeight() / 2f + 10 - 10, font, 14f, Font.Align.CENTER, p);
            }
        }

        { // xp
            float xp = mc.thePlayer.experience;

            smoothXP = MathHelper.lerp(smoothXP, xp, lerpDelta);

            if (Float.isNaN(smoothXP) || Float.isInfinite(smoothXP)) {
                smoothXP = xp;
            }

            try (Paint p = new Paint()) {
                p.setColor(0xFF242424);
                Renderer.drawRRect(xpNode.getX(), xpNode.getY(), xpNode.getWidth(), xpNode.getHeight(), xpNode.getHeight() / 2f, p);

                float stroke = 2f;
                p.setColor(0xFF5afad1);

                PathBuilder pb = new PathBuilder();
                pb.moveTo(xpNode.getX() + xpNode.getWidth() / 2f, xpNode.getY() + xpNode.getHeight() / 2f);
                pb.arcTo(Rect.makeXYWH(xpNode.getX() + stroke, xpNode.getY() + stroke, xpNode.getWidth() - stroke * 2, xpNode.getHeight() - stroke * 2), 90, smoothXP * 360, false);
                canvas.drawPath(pb.build(), p);

                stroke = 4f;
                p.setColor(0xFF242424);
                Renderer.drawRRect(xpNode.getX() + stroke, xpNode.getY() + stroke, xpNode.getWidth() - stroke * 2, xpNode.getHeight() - stroke * 2, xpNode.getHeight() / 2f, p);
            }

            int level = mc.thePlayer.experienceLevel;

            try (Paint p = new Paint()) {
                p.setColor(0xFF5afad1);
                Renderer.drawText(String.valueOf(level), xpNode.getX() + xpNode.getWidth() / 2f, xpNode.getY() + xpNode.getHeight() / 2f - 1, Fonts.InpinHongmengti, 13f, Font.Align.CENTER, p);
            }
        }

        { // hotbar
            for (int i = 0; i < 9; i++) {
                ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);

                boolean selected = mc.thePlayer.inventory.currentItem == i;
                float stroke = 3f;

                slots.get(i).selectAnimation.doEase(selected);
                if (selected) {
                    slots.get(i).selectAnimation.forceFinish();
                }

                Node slot = slotNodes[i];

                try (Paint p = new Paint()) {
                    p.setColor(0xFF282828);
                    Renderer.drawRRect(slot.getX(), slot.getY(), slot.getWidth(), slot.getHeight(), 12f, p);
                }
                try (Paint p = new Paint()) {
                    if (item != null && item.isItemDamaged()) {
                        float durability = (float) item.getItemDamage() / item.getMaxDamage();
                        float currentHealth = 1f - durability;

                        float healthHue = currentHealth * 0.27f;
                        int healthColor = java.awt.Color.HSBtoRGB(healthHue, 0.65f + 0.1f * durability, 1f);

                        p.setColor(healthColor/*0xFF5afad1*/);
                        Renderer.save();
                        Renderer.clipRRect(slot.getX() + stroke, slot.getY() + stroke, slot.getWidth() - stroke * 2, slot.getHeight() - stroke * 2, 12f - stroke);
                        PathBuilder pb = new PathBuilder();
                        pb.moveTo(slot.getX() + slot.getWidth() / 2f, slot.getY() + slot.getHeight() / 2f);
                        pb.arcTo(Rect.makeXYWH(slot.getX() - 4f, slot.getY() - 4f, slot.getWidth() + 8f, slot.getHeight() + 8f), 90, currentHealth * 360, false);
                        canvas.drawPath(pb.build(), p);
                        Renderer.restore();
                        stroke += 3;
                    }
                }

                try (Paint p = new Paint()) {
                    p.setColor(0xFF343434);
                    Renderer.drawRRect(slot.getX() + stroke, slot.getY() + stroke, slot.getWidth() - stroke * 2, slot.getHeight() - stroke * 2, 12f - stroke, p);

                    float slotAnim = (float) slots.get(i).selectAnimation.getValue();

                    p.setStroke(true);

                    stroke = -2;
                    p.setColor(Color.makeLerp(0x00000000, 0xFF000000, slotAnim));
                    p.setStrokeWidth(2f * slotAnim);
                    Renderer.drawRRect(slot.getX() + stroke, slot.getY() + stroke, slot.getWidth() - stroke * 2, slot.getHeight() - stroke * 2, 12f - stroke, p);

                    stroke = 0f;
                    p.setColor(Color.makeLerp(0x00FFFFFF, -1, slotAnim));
                    p.setStrokeWidth(3f * slotAnim);
                    Renderer.drawRRect(slot.getX() + stroke, slot.getY() + stroke, slot.getWidth() - stroke * 2, slot.getHeight() - stroke * 2, 12f - stroke, p);
                }

                canvas.drawRectShadowNoclip(Rect.makeXYWH(slot.getX() + 8, slot.getY() + 8, slot.getWidth() - 16, slot.getHeight() - 16), 0f, 0f, 10f, 0f, 0x88000000);
            }
        }
    }

    @Subscribe
    public void oe$TopSkia(EventHUD.TopSkia event) {
        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);

            Node slot = slotNodes[i];

            try (Paint p = new Paint()) {
                if (item != null && item.stackSize > 1) {
                    p.setStroke(true);
                    p.setStrokeWidth(2f);
                    p.setColor(0xFF000000);
                    Renderer.drawText(item.stackSize + "", slot.getX() + slot.getWidth() - 6, slot.getY() + slot.getHeight() - 6, Fonts.Rubik.weight(500), 14f, Font.Align.BOTTOM_RIGHT, p);
                    p.setColor(-1);
                    p.setStroke(false);
                    Renderer.drawText(item.stackSize + "", slot.getX() + slot.getWidth() - 6, slot.getY() + slot.getHeight() - 6, Fonts.Rubik.weight(500), 14f, Font.Align.BOTTOM_RIGHT, p);
                }
            }
        }
    }

    @Subscribe
    public void oe$TopVanilla(EventHUD.TopVanilla event) {
        Renderer2D.begin2d();
        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
            Node slot = slotNodes[i];

            float itemX = slot.getX() + (slot.getWidth() - 32) / 2f;
            float itemY = slot.getY() + (slot.getHeight() - 32) / 2f;

            if (item != null) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(2f, 2f, 1f);
                Renderer2D.drawItem(item, itemX / 2, itemY / 2);
                GlStateManager.popMatrix();
            }
        }
        Renderer2D.end2d();
    }

    private static class HotbarSlot {
        Animate selectAnimation = new Animate(160L, Easing.LINEAR);
    }
}
