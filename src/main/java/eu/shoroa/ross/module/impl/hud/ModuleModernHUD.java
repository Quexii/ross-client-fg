package eu.shoroa.ross.module.impl.hud;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.EventInGameHUD;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.settings.BooleanSetting;
import eu.shoroa.ross.types.Size;
import eu.shoroa.ross.util.render.Renderer2D;
import io.github.humbleui.skija.*;
import io.github.humbleui.types.RRect;
import io.github.humbleui.types.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldSettings;
import org.lwjgl.opengl.Display;

import static eu.shoroa.ross.Client.mc;

// evil characters -> `ᐸᐳ`
public class ModuleModernHUD extends Module {
    private final BooleanSetting changeStats = register(new BooleanSetting("Change Stats", "modernhud.changehud", false));
    private final BooleanSetting changeHotbar = register(new BooleanSetting("Change Hotbar", "modernhud.changehotbar", false));
    private final BooleanSetting changeXP = register(new BooleanSetting("Change XP Bar", "modernhud.changexp", false));

    public ModuleModernHUD() {
        super("Modern HUD", "Gacha-styled HUD replacement.", Category.HUD);
    }

    @Subscribe
    public void oe$HideVanillaStats(EventInGameHUD.Stats event) {
        event.setCanceled(changeStats.get());
    }

    @Subscribe
    public void oe$HideVanillaHotbar(EventInGameHUD.Hotbar event) {
        event.setCanceled(changeHotbar.get());
    }

    @Subscribe
    public void oe$HideVanillaXP(EventInGameHUD.XP event) {
        event.setCanceled(changeXP.get());
    }

    @Subscribe
    public void oe$Hud(EventHUD.BottomSkia event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        Canvas canvas = Client.INSTANCE.skia.getCanvas();
        if (canvas == null) return;

        if (changeStats.get()) {
            renderStats(canvas);
        }

        if (changeHotbar.get()) {
            renderHotbar(canvas);
        }

        if (changeXP.get()) {
            renderXP(canvas);
        }
    }

    @Subscribe
    public void oe$HudTop(EventHUD.TopSkia event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        Canvas canvas = Client.INSTANCE.skia.getCanvas();
        if (canvas == null) return;

        if (changeHotbar.get()) {
            renderHotbarTop(canvas);
        }
    }

    @Subscribe
    public void oe$HudVanilla(EventHUD.TopVanilla event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (changeHotbar.get()) {
            renderHotbarVanilla();
        }
    }

    private void renderStats(Canvas canvas) {
        Size screen = new Size(Display.getWidth(), Display.getHeight());
        float w = screen.width;
        float h = screen.height;

        NetworkPlayerInfo npi = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(mc.thePlayer.getGameProfile().getId());
        if (!mc.thePlayer.isSpectator() && !npi.getGameType().equals(WorldSettings.GameType.CREATIVE)) {
            final float barWidth = 320;
            final float barHeight = 20;
            float health = mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth();
            float absorption = mc.thePlayer.getAbsorptionAmount() / mc.thePlayer.getMaxHealth();

            float r = 10f;
            float y = h - 38;
            if (!changeHotbar.get()) y -= 30;
            if (!changeXP.get()) y -= 24;

            try (Paint p = new Paint()) {
                p.setColor(0xFF282828);
                Renderer.drawRRect((w - barWidth) / 2f, y, barWidth, barHeight, r, p);

                float stroke = 2f;
                p.setColor(0xFF343434);
                Renderer.drawRRect((w - barWidth) / 2f + stroke, y + stroke, (barWidth - stroke * 2), barHeight - stroke * 2, r, p);

                Renderer.save();
                canvas.clipRRect(RRect.makeXYWH((w - barWidth) / 2f + stroke, y + stroke, (barWidth - stroke * 2), barHeight - stroke * 2, r), true);
                p.setColor(0xFF5afad1);
                Renderer.drawRect((w - barWidth) / 2f + stroke, y + stroke, (barWidth - stroke * 2) * health, barHeight - stroke * 2, p);
                Renderer.restore();

                p.setColor(0xFF282828);
                Renderer.drawRRect((w - barWidth) / 2f + 3, y, (barWidth) * absorption, barHeight, r, p);
                p.setColor(0xFFfcfa8a);
                Renderer.drawRRect((w - barWidth) / 2f + stroke, y + stroke, (barWidth - stroke * 2) * absorption, barHeight - stroke * 2, r, p);
            }

            try (Paint p = new Paint()) {
                Font font = Fonts.Geist.weight(600);

                p.setImageFilter(ImageFilter.makeDropShadow(0f, 0f, 3f, 3f, 0xAA000000));
                p.setStroke(true);
                p.setStrokeWidth(2f);
                p.setColor(0xFF000000);
                Renderer.drawText((int) ((mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) / 20f * 100f) + " / " + (int) (mc.thePlayer.getMaxHealth() / 20f * 100f), w / 2f, y + barHeight / 2f + 10 - 10, font, 14f, Font.Align.CENTER, p);
                p.setColor(-1);
                p.setStroke(false);
                p.setImageFilter(null);
                Renderer.drawText((int) ((mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) / 20f * 100f) + " / " + (int) (mc.thePlayer.getMaxHealth() / 20f * 100f), w / 2f, y + barHeight / 2f + 10 - 10, font, 14f, Font.Align.CENTER, p);
            }
        }
    }

    private void renderHotbar(Canvas canvas) {
        Size screen = new Size(Display.getWidth(), Display.getHeight());
        float w = screen.width;
        float h = screen.height;

        float slotSize = 42f;
        float slotGap = 4f;
        float y = h - slotSize - 8f;

        if (changeStats.get()) {
            y -= 44f;
        }

        for (int i = 0; i < 9; i++) {
            float x = (w - (slotSize * 9 + slotGap * 8)) / 2f + i * (slotSize + slotGap);

            ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);

            boolean selected = mc.thePlayer.inventory.currentItem == i;

            try (Paint p = new Paint()) {
                p.setColor(0xFF282828);
                Renderer.drawRRect(x, y, slotSize, slotSize, 12f, p);

                float stroke = 3f;
                if (item != null && item.isItemDamaged()) {
                    float durability = (float) item.getItemDamage() / item.getMaxDamage();
                    float currentHealth = 1f - durability;

                    float healthHue = currentHealth * 0.27f;
                    int healthColor = java.awt.Color.HSBtoRGB(healthHue, 0.65f + 0.1f * durability, 1f);

                    p.setColor(healthColor/*0xFF5afad1*/);
                    Renderer.save();
                    canvas.clipRRect(RRect.makeXYWH(x + stroke, y + stroke, slotSize - stroke * 2, slotSize - stroke * 2, 12f - stroke), true);
                    PathBuilder pb = new PathBuilder();
                    pb.moveTo(x + slotSize / 2f, y + slotSize / 2f);
                    pb.arcTo(Rect.makeXYWH(x - 4f, y - 4f, slotSize + 8f, slotSize + 8f), 90, currentHealth * 360, false);
                    canvas.drawPath(pb.build(), p);
                    Renderer.restore();
                    stroke += 3;
                }
                p.setColor(0xFF343434);
                Renderer.drawRRect(x + stroke, y + stroke, slotSize - stroke * 2, slotSize - stroke * 2, 12f - stroke, p);

                if (selected) {
                    stroke = -2f;
                    p.setColor(0xFFFFFFFF);
                    p.setStroke(true);
                    p.setStrokeWidth(3f);
                    Renderer.drawRRect(x + stroke, y + stroke, slotSize - stroke * 2, slotSize - stroke * 2, 12f - stroke, p);
                }
            }

            canvas.drawRectShadowNoclip(Rect.makeXYWH(x + 8, y + 8, slotSize - 16, slotSize - 16), 0f, 0f, 10f, 0f, 0x88000000);
        }
    }

    private void renderHotbarTop(Canvas canvas) {
        Size screen = new Size(Display.getWidth(), Display.getHeight());
        float w = screen.width;
        float h = screen.height;

        float slotSize = 42f;
        float slotGap = 4f;
        float y = h - slotSize - 8f;

        if (changeStats.get()) {
            y -= 44f;
        }

        for (int i = 0; i < 9; i++) {
            float x = (w - (slotSize * 9 + slotGap * 8)) / 2f + i * (slotSize + slotGap);
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);

            try (Paint p = new Paint()) {
                if (item != null && item.stackSize > 1) {
                    p.setStroke(true);
                    p.setStrokeWidth(2f);
                    p.setColor(0xFF000000);
                    Renderer.drawText(item.stackSize + "", x + slotSize - 6, y + slotSize - 6, Fonts.Rubik.weight(500), 14f, Font.Align.BOTTOM_RIGHT, p);
                    p.setColor(-1);
                    p.setStroke(false);
                    Renderer.drawText(item.stackSize + "", x + slotSize - 6, y + slotSize - 6, Fonts.Rubik.weight(500), 14f, Font.Align.BOTTOM_RIGHT, p);
                }
            }
        }
    }

    private void renderHotbarVanilla() {
        Size screen = new Size(Display.getWidth(), Display.getHeight());
        float w = screen.width;
        float h = screen.height;

        float slotSize = 42f;
        float slotGap = 4f;
        float y = h - slotSize - 8f;

        if (changeStats.get()) {
            y -= 44f;
        }

        Renderer2D.begin2d();
        for (int i = 0; i < 9; i++) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);

            float x = (w - (slotSize * 9 + slotGap * 8)) / 2f + i * (slotSize + slotGap);

            float itemX = x + (slotSize - 32) / 2f;
            float itemY = y + (slotSize - 32) / 2f;

            if (item != null) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(2f, 2f, 1f);
                Renderer2D.drawItem(item, itemX / 2, itemY / 2, false);
                GlStateManager.popMatrix();
            }
        }
        Renderer2D.end2d();
    }

    private void renderXP(Canvas canvas) {
        Size screen = new Size(Display.getWidth(), Display.getHeight());
        float w = screen.width;
        float h = screen.height;

        float size = 32f;
        float x = (w - size) / 2;
        float y = h - 48f - size;

        if (changeStats.get()) {
            x = (w - 320) / 2f - size / 4 + 2;
            y = h - 38 - size - 4;

            if (changeHotbar.get()) {
                y += 30;
            }
        }

        float xp = mc.thePlayer.experience;

        try (Paint p = new Paint()) {
            p.setColor(0xFF242424);
            Renderer.drawRRect(x, y, size, size, size / 2f, p);

            float stroke = 2f;
            p.setColor(0xFF5afad1);

            PathBuilder pb = new PathBuilder();
            pb.moveTo(x + size / 2f, y + size / 2f);
            pb.arcTo(Rect.makeXYWH(x + stroke, y + stroke, size - stroke * 2, size - stroke * 2), 90, xp * 360, false);
            canvas.drawPath(pb.build(), p);

            stroke = 4f;
            p.setColor(0xFF242424);
            Renderer.drawRRect(x + stroke, y + stroke, size - stroke * 2, size - stroke * 2, size / 2f, p);
        }

        int level = mc.thePlayer.experienceLevel;

        try (Paint p = new Paint()) {
            p.setColor(0xFF5afad1);
            Renderer.drawText(level + "", x + size / 2f, y + size / 2f - 1, Fonts.InpinHongmengti, 13f, Font.Align.CENTER, p);
        }
    }
}