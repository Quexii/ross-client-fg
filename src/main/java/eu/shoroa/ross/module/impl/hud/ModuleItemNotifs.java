package eu.shoroa.ross.module.impl.hud;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.image.Images;
import io.github.humbleui.skija.BlendMode;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Shader;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayDeque;

import static eu.shoroa.ross.Client.mc;

public class ModuleItemNotifs extends Module {
    private final ArrayDeque<ItemNotif> queue = new ArrayDeque<>();

    private final Object queueLock = new Object();

    public ModuleItemNotifs() {
        super("Item Notifier", "Notifies you when you pick up an item.", Category.HUD, null);
    }

    @Subscribe
    public void oe$ItemPickedUp(EntityItemPickupEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event.entityPlayer.getEntityId() != mc.thePlayer.getEntityId()) return;

        System.out.println("Item picked up: " + event.item.getAge());

        ItemStack stack = event.item.getEntityItem();
        String itemName = stack.getDisplayName();
        int count = stack.stackSize;

        synchronized (queueLock) {
            queue.add(new ItemNotif(itemName, count, stack));
        }
    }

    @Subscribe
    public void oe$Hud(EventHUD.BottomSkia event) {
        if (queue.isEmpty()) return;

        float notifX = 10f;
        float notifY = 40f;
        float width = 160f;
        float height = 40f;

        float yOffset = 0;
        int max = 5;

        Canvas canvas = Client.INSTANCE.skia.getCanvas();
        if (canvas == null) return;

        ItemNotif[] snapshot;
        synchronized (queueLock) {
            snapshot = queue.toArray(new ItemNotif[0]);
        }

        int i = 0;
        for (ItemNotif notif : snapshot) {
            if (i++ >= max) break;

            float x = notifX - width * (float) (1f - (notif.entryAnim.getValue() - notif.exitAnim.getValue()));
            float y = notifY + yOffset;

            canvas.saveLayerAlpha(null, (int) ((notif.entryAnim.getValue() - notif.exitAnim.getValue()) * 255));
            renderSkia(notif, x, y, width, height, canvas);
            canvas.restore();

            yOffset += (height + 8) * (notif.entryAnim.getValue() - notif.exitAnim.getValue());
        }

        synchronized (queueLock) {
            queue.removeIf((n) -> n.isExpired() && n.exitAnim.getValue() >= 0.99);
        }
    }

    private void renderSkia(ItemNotif notif, float x, float y, float width, float height, Canvas canvas) {
        int color = 0xFFfbbf67;//0xFF4CAF50;
        int colorGlint = 0xFFffe082;//0xFF4CAF50;
        float alpha = 1.0f;
        float glintAlpha = (float) (1f - notif.entryAnim.getValue());

        int gradColor = (color & 0x00FFFFFF) | (int) (alpha * 255) << 24;
        int glintColor = (colorGlint & 0x00FFFFFF) | (int) (glintAlpha * 255) << 24;

        Font font = Fonts.GoogleFlex.weight(500).opticSize(14);

        String text = String.format("[%s] %d", notif.title, notif.count);

        float textWidth = Renderer.getTextBounds(text, font, 14f).width;

        width = Math.max(width, textWidth + 80f);

        try (Paint p = new Paint()) {
            canvas.saveLayer(null, null);

            p.setColor(-1);
            Renderer.drawFilter(Filter.kawase(), mc.getFramebuffer().framebufferTexture, x, y, width, height, 8);
            p.setColor(0x44000000);
            Renderer.drawRRect(x, y, width, height, height / 2f, p);
            p.setBlendMode(BlendMode.DST_IN);

            p.setColor(-1);
            Renderer.drawRect(x, y, width - 80, height, p);
            Renderer.drawImage(Images.HALFTONE_RECT, x + width - 80, y - 12, Images.HALFTONE_RECT.getWidth() / 4f, Images.HALFTONE_RECT.getHeight() / 4f, p);
            p.setBlendMode(BlendMode.SRC_OVER);

            p.setShader(Shader.makeLinearGradient(x, y, x + 100, y, new int[]{gradColor, gradColor & 0x00FFFFFF}, null));
            Renderer.drawRRect(x, y, width, height, height, p);
            p.setShader(Shader.makeLinearGradient(x, y, x + 100, y, new int[]{glintColor, glintColor & 0x00FFFFFF}, null));
            Renderer.drawRRect(x, y, width, height, height, p);
            p.setShader(null);
            canvas.restore();

            p.setColor(0xFF242424);
            Renderer.drawRRect(x, y, height, height, height / 2f, p);

            p.setColor(gradColor);
            p.setStroke(true);
            p.setStrokeWidth(2.5f);
            Renderer.drawRRect(x, y, height, height, height / 2f, p);

            p.setStroke(false);
            p.setColor(0xFFFFFFFF);
            Renderer.drawText(text, x + 46f, y + height / 2f, font, 14f, Font.Align.CENTER_LEFT, p);
        }
    }

    private class ItemNotif {
        private final String title;
        private final int count;
        private final ItemStack stack;

        private long startTime;
        private final long duration = 200000;

        private final long exitAnimTime = 200;

        private final Animate entryAnim = new Animate(200, Easing.QUART_OUT).easeIf(() -> true);
        private final Animate exitAnim = new Animate(exitAnimTime, Easing.QUART_IN).easeIf(() -> getTime() >= duration - exitAnimTime);

        public ItemNotif(String title, int count, ItemStack stack) {
            this.title = title;
            this.count = count;
            this.stack = stack;
            this.startTime = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return getTime() > duration;
        }

        public long getTime() {
            return System.currentTimeMillis() - startTime;
        }

        public float getProgress() {
            return Math.min(1f, getTime() / (float) duration);
        }
    }
}
//package eu.shoroa.ross.module.impl.hud;
//
//import eu.shoroa.ross.Client;
//import eu.shoroa.ross.animate.Animate;
//import eu.shoroa.ross.animate.Easing;
//import eu.shoroa.ross.event.EventHUD;
//import eu.shoroa.ross.module.Category;
//import eu.shoroa.ross.module.Module;
//import eu.shoroa.ross.render.Renderer;
//import eu.shoroa.ross.render.filters.Filter;
//import eu.shoroa.ross.render.skia.font.Font;
//import eu.shoroa.ross.render.skia.font.Fonts;
//import io.github.humbleui.skija.*;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.client.renderer.RenderHelper;
//import net.minecraft.item.ItemStack;
//import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//
//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Random;
//
//import static eu.shoroa.ross.Client.mc;
//
///**
// * Vanta Loot — Gacha-game inspired item notification system.
// *
// * Elegant glassmorphism cards slide in from the right edge with rarity-based
// * prismatic glows, diamond item badges, and floating particles for premium drops.
// * Inspired by Stella Sora's clean anime-polished HUD aesthetic.
// */
//public class ModuleItemNotifs extends Module {
//
//    // ═══════════════════════════════════════════════════════════
//    //  CONFIGURATION
//    // ═══════════════════════════════════════════════════════════
//
//    private static final int    CARD_WIDTH_BASE     = 175;
//    private static final int    CARD_HEIGHT         = 52;
//    private static final int    CARD_RADIUS         = 18;
//    private static final int    CARD_GAP            = 10;
//    private static final int    CARD_RIGHT_MARGIN   = 15;
//    private static final int    MAX_VISIBLE         = 4;
//
//    private static final long   ENTRY_DURATION      = 400;
//    private static final long   HOLD_DURATION       = 3500;
//    private static final long   EXIT_DURATION       = 400;
//    private static final long   TOTAL_DURATION      = HOLD_DURATION + EXIT_DURATION;
//
//    private static final float  ENTRY_SCALE_FROM    = 0.85f;
//    private static final float  GLOW_ALPHA_BASE     = 0.22f;
//    private static final float  GLOW_ALPHA_PULSE    = 0.13f;
//    private static final long   GLOW_PULSE_PERIOD   = 2000;
//
//    private static final int    DIAMOND_SIZE        = 38;
//    private static final int    DIAMOND_INNER_SIZE  = 30;
//    private static final int    ACCENT_BAR_WIDTH    = 4;
//
//    private static final int    PARTICLE_COUNT_RARE = 2;
//    private static final int    PARTICLE_COUNT_EPIC = 3;
//    private static final int    PARTICLE_COUNT_LEG  = 5;
//    private static final float  PARTICLE_SPEED      = 8f;
//
//    private static final int    STAR_SIZE           = 7;
//    private static final int    STAR_GAP            = 3;
//
//    // ═══════════════════════════════════════════════════════════
//    //  RARITY SYSTEM
//    // ═══════════════════════════════════════════════════════════
//
//    private enum Rarity {
//        COMMON(   0xFF9E9E9E, 0xFF757575, 0xFFBDBDBD, 0),
//        UNCOMMON( 0xFF66BB6A, 0xFF43A047, 0xFF81C784, 0),
//        RARE(     0xFF42A5F5, 0xFF1E88E5, 0xFF64B5F6, 2),
//        EPIC(     0xFFAB47BC, 0xFF8E24AA, 0xFFBA68C8, 3),
//        LEGENDARY(0xFFFFA726, 0xFFFF7043, 0xFFFFB74D, 5);
//
//        final int primary, dark, light;
//        final int stars;
//
//        Rarity(int primary, int dark, int light, int stars) {
//            this.primary = primary;
//            this.dark = dark;
//            this.light = light;
//            this.stars = stars;
//        }
//
//        static Rarity fromItem(ItemStack stack) {
//            if (stack == null) return COMMON;
//            String name = stack.getDisplayName().toLowerCase();
//            // Heuristic rarity detection from item properties
//            if (stack.hasEffect() || name.contains("legendary") || name.contains("ancient")
//                    || name.contains("divine") || name.contains("mythic")) {
//                return LEGENDARY;
//            }
//            if (stack.isItemEnchanted() || name.contains("epic") || name.contains("rare")) {
//                // Check enchantment level for fine-grained rarity
//                if (stack.getItemDamage() == 0 && stack.isItemEnchanted()) {
//                    return EPIC;
//                }
//                return RARE;
//            }
//            if (name.contains("uncommon") || name.contains("unusual")) {
//                return UNCOMMON;
//            }
//            // Default: use item durability/maxDamage as hint
//            if (stack.getMaxDamage() > 500 || stack.getMaxStackSize() == 1) {
//                return RARE;
//            }
//            return COMMON;
//        }
//    }
//
//    // ═══════════════════════════════════════════════════════════
//    //  STATE
//    // ═══════════════════════════════════════════════════════════
//
//    private final ArrayDeque<ItemNotif> queue = new ArrayDeque<>();
//    private final Object queueLock = new Object();
//    private final Random rng = new Random();
//
//    public ModuleItemNotifs() {
//        super("Item Notifier", "Notifies you when you pick up an item.", Category.HUD, null);
//    }
//
//    // ═══════════════════════════════════════════════════════════
//    //  EVENT HANDLERS
//    // ═══════════════════════════════════════════════════════════
//
//    @SubscribeEvent
//    public void onItemPickup(EntityItemPickupEvent event) {
//        if (mc.thePlayer == null || mc.theWorld == null) return;
//        if (event.entityPlayer.getEntityId() != mc.thePlayer.getEntityId()) return;
//
//        ItemStack stack = event.item.getEntityItem();
//        String itemName = stack.getDisplayName();
//        int count = stack.stackSize;
//
//        synchronized (queueLock) {
//            queue.add(new ItemNotif(itemName, count, stack));
//        }
//    }
//
//    @SubscribeEvent
//    public void onHudRender(EventHUD.BottomSkia event) {
//        if (queue.isEmpty()) return;
//
//        Canvas canvas = Client.INSTANCE.skia.getCanvas();
//        if (canvas == null) return;
//
//        ItemNotif[] snapshot;
//        synchronized (queueLock) {
//            snapshot = queue.toArray(new ItemNotif[0]);
//        }
//
//        float screenWidth = mc.displayWidth;
//        float cardRestX = screenWidth - CARD_WIDTH_BASE - CARD_RIGHT_MARGIN;
//
//        float yOffset = 0;
//        int rendered = 0;
//
//        for (ItemNotif notif : snapshot) {
//            if (rendered++ >= MAX_VISIBLE) break;
//
//            float globalAlpha = (float) (notif.entryAnim.getValue() - notif.exitAnim.getValue());
//            if (globalAlpha <= 0.01f) continue;
//
//            // Entry slide: from off-screen right to rest position
//            float entryProgress = (float) notif.entryAnim.getValue();
//            float slideInX = (screenWidth + CARD_WIDTH_BASE + 30) * (1f - entryProgress);
//            float scale = ENTRY_SCALE_FROM + (1f - ENTRY_SCALE_FROM) * entryProgress;
//
//            // Exit slide: drift further right
//            float exitProgress = (float) notif.exitAnim.getValue();
//            float slideOutX = (CARD_WIDTH_BASE + 30) * exitProgress;
//
//            float x = cardRestX + slideInX + slideOutX;
//            float y = 80f + yOffset; // Start below the top of screen
//
//            canvas.saveLayerAlpha(null, (int) (globalAlpha * 255));
//            canvas.save();
//
//            // Apply scale from card center
//            float cx = x + CARD_WIDTH_BASE / 2f;
//            float cy = y + CARD_HEIGHT / 2f;
//            canvas.translate(cx, cy);
//            canvas.scale(scale, scale);
//            canvas.translate(-cx, -cy);
//
//            renderCard(notif, x, y, CARD_WIDTH_BASE, CARD_HEIGHT, canvas);
//
//            canvas.restore();
//            canvas.restore();
//
//            // Stack upward — older notifications push up
//            float spacing = (CARD_HEIGHT + CARD_GAP) * globalAlpha;
//            yOffset += spacing;
//        }
//
//        // Cleanup expired notifications
//        synchronized (queueLock) {
//            queue.removeIf((n) -> n.isExpired() && n.exitAnim.getValue() >= 0.99);
//        }
//    }
//
//    // ═══════════════════════════════════════════════════════════
//    //  CARD RENDERING
//    // ═══════════════════════════════════════════════════════════
//
//    private void renderCard(ItemNotif notif, float x, float y, float width, float height, Canvas canvas) {
//        Rarity rarity = notif.rarity;
//        long time = notif.getTime();
//
//        // Calculate animated glow alpha (pulse during hold phase)
//        float glowPulse = 0f;
//        if (time >= ENTRY_DURATION && time < HOLD_DURATION) {
//            long holdTime = time - ENTRY_DURATION;
//            glowPulse = (float) Math.sin(holdTime * 2 * Math.PI / GLOW_PULSE_PERIOD) * 0.5f + 0.5f;
//        }
//        float glowAlpha = GLOW_ALPHA_BASE + GLOW_ALPHA_PULSE * glowPulse;
//
//        // Measure text to auto-size card
//        Font nameFont = Fonts.GoogleFlex.weight(500).opticSize(13);
//        Font countFont = Fonts.GoogleFlex.weight(500).opticSize(10);
//        String nameText = notif.title;
//        String countText = "x" + notif.count;
//
//        float nameWidth = Renderer.getTextBounds(nameText, nameFont, 13f).width;
//        float countWidth = Renderer.getTextBounds(countText, countFont, 10f).width;
//        float totalTextWidth = Math.max(nameWidth, countWidth);
//        float minContentWidth = DIAMOND_SIZE + 18 + totalTextWidth + 20;
//        if (rarity.stars > 0) {
//            minContentWidth = Math.max(minContentWidth,
//                    DIAMOND_SIZE + 18 + rarity.stars * (STAR_SIZE + STAR_GAP));
//        }
//        width = Math.max(width, minContentWidth);
//
//        try (Paint p = new Paint()) {
//
//            // ── Layer 1: Outer Glow ──
//            if (glowAlpha > 0.01f) {
//                p.setColor(rarity.primary);
//                p.setAlpha((int) (glowAlpha * 180));
//                float glowExpand = 3f + 2f * glowPulse;
//                Renderer.drawRRect(x - glowExpand, y - glowExpand,
//                        width + glowExpand * 2, height + glowExpand * 2,
//                        CARD_RADIUS + glowExpand, p);
//            }
//
//            // ── Layer 2: Kawase Blur Glassmorphism ──
//            canvas.saveLayer(null, null);
//            p.setColor(-1);
//            Renderer.drawFilter(Filter.kawase(), mc.getFramebuffer().framebufferTexture,
//                    x, y, width, height, 6);
//
//            p.setColor(0xE6181828);
//            Renderer.drawRRect(x, y, width, height, CARD_RADIUS, p);
//
//            // Blend the blur into the card shape
//            p.setBlendMode(BlendMode.DST_IN);
//            p.setColor(-1);
//            Renderer.drawRRect(x, y, width, height, CARD_RADIUS, p);
//            p.setBlendMode(BlendMode.SRC_OVER);
//            canvas.restore();
//
//            // ── Layer 3: Left Accent Gradient Bar ──
//            p.setColor(rarity.primary);
//            Renderer.drawRRect(x, y, ACCENT_BAR_WIDTH, height, ACCENT_BAR_WIDTH / 2f, p);
//
//            // ── Layer 4: Subtle Horizontal Gradient Overlay ──
//            int[] gradColors = new int[]{
//                    (rarity.primary & 0x00FFFFFF) | 0x10000000,
//                    0x00181828
//            };
//            p.setShader(Shader.makeLinearGradient(x, y, x + width * 0.5f, y, gradColors, null));
//            Renderer.drawRRect(x, y, width, height, CARD_RADIUS, p);
//            p.setShader(null);
//
//            // ── Layer 5: Inner Glow Border ──
//            float borderAlpha = 0.35f + 0.15f * glowPulse;
//            p.setColor(rarity.light);
//            p.setAlpha((int) (borderAlpha * 255));
//            p.setStroke(true);
//            p.setStrokeWidth(1.2f);
//            Renderer.drawRRect(x + 1, y + 1, width - 2, height - 2, CARD_RADIUS - 1, p);
//            p.setStroke(false);
//
//            // ── Layer 6: Diamond Icon Container ──
//            float diamondCx = x + 28;
//            float diamondCy = y + height / 2f;
//            drawDiamond(canvas, diamondCx, diamondCy, DIAMOND_SIZE, rarity.dark, p);
//            drawDiamond(canvas, diamondCx, diamondCy, DIAMOND_INNER_SIZE, 0xFF252535, p);
//
//            // ── Layer 7: Minecraft Item Icon ──
//            renderItemIcon(notif.stack, diamondCx - DIAMOND_INNER_SIZE / 2f + 1,
//                    diamondCy - DIAMOND_INNER_SIZE / 2f + 1, DIAMOND_INNER_SIZE - 2);
//
//            // ── Layer 8: Text ──
//            float textX = x + 56;
//            float textY = y + height / 2f - 3;
//
//            // Item name
//            p.setColor(0xFFFFFFFF);
//            Renderer.drawText(nameText, textX, textY, nameFont, 13f, Font.Align.CENTER_LEFT, p);
//
//            // Count label
//            p.setColor(rarity.light);
//            Renderer.drawText(countText, textX, textY + 15, countFont, 10f, Font.Align.CENTER_LEFT, p);
//
//            // ── Layer 9: Rarity Stars ──
//            if (rarity.stars > 0) {
//                float starsWidth = rarity.stars * STAR_SIZE + (rarity.stars - 1) * STAR_GAP;
//                float starX = textX;
//                float starY = textY + 19;
//                for (int i = 0; i < rarity.stars; i++) {
//                    float sx = starX + i * (STAR_SIZE + STAR_GAP);
//                    float twinkle = (rarity == Rarity.LEGENDARY)
//                            ? (float) (0.7 + 0.3 * Math.sin(time * 0.005 + i * 1.2))
//                            : 1f;
//                    drawStar(canvas, sx, starY, STAR_SIZE / 2f, rarity.primary, twinkle, p);
//                }
//            }
//
//            // ── Layer 10: Legendary Shimmer Sweep ──
//            if (rarity == Rarity.LEGENDARY && time > ENTRY_DURATION + 300) {
//                long shimmerTime = time - ENTRY_DURATION - 300;
//                if (shimmerTime < 1200) {
//                    float shimmerProgress = shimmerTime / 1200f;
//                    float shimmerX = x - width + shimmerProgress * (width * 3);
//                    int[] shimmerColors = new int[]{0x00FFFFFF, 0x18FFFFFF, 0x00FFFFFF};
//                    float[] shimmerPos = new float[]{0f, 0.5f, 1f};
//                    p.setShader(Shader.makeLinearGradient(shimmerX - 40, y, shimmerX + 40, y + height,
//                            shimmerColors, shimmerPos));
//                    Renderer.drawRRect(x, y, width, height, CARD_RADIUS, p);
//                    p.setShader(null);
//                }
//            }
//
//            // ── Layer 11: Particles (Epic+) ──
//            if (rarity.stars >= 2) {
//                updateParticles(notif, time);
////                notif.updateParticles(time);
//                for (Particle particle : notif.particles) {
//                    if (particle.alpha <= 0.01f) continue;
//                    p.setColor(rarity.light);
//                    p.setAlpha((int) (particle.alpha * 200));
//                    drawSparkle(canvas, particle.x, particle.y, particle.size, p);
//                }
//            }
//        }
//    }
//
//    // ═══════════════════════════════════════════════════════════
//    //  SHAPE HELPERS
//    // ═══════════════════════════════════════════════════════════
//
//    /**
//     * Draws a diamond shape (rotated square) centered at (cx, cy).
//     */
//    private void drawDiamond(Canvas canvas, float cx, float cy, float size, int color, Paint paint) {
//        paint.setColor(color);
//        float half = size / 2f;
//        PathBuilder builder = new PathBuilder();
//        builder.moveTo(cx, cy - half);
//        builder.lineTo(cx + half, cy);
//        builder.lineTo(cx, cy + half);
//        builder.lineTo(cx - half, cy);
//        builder.closePath();
//        canvas.drawPath(builder.build(), paint);
//    }
//
//    /**
//     * Draws a 5-point star centered at (cx, cy) with given radius.
//     */
//    private void drawStar(Canvas canvas, float cx, float cy, float outerR, int color, float alpha, Paint paint) {
//        paint.setColor(color);
//        paint.setAlpha((int) (alpha * 255));
//        float innerR = outerR * 0.4f;
//
//        PathBuilder builder = new PathBuilder();
//        for (int i = 0; i < 10; i++) {
//            float angle = (float) (Math.PI * i / 5 - Math.PI / 2);
//            float r = (i % 2 == 0) ? outerR : innerR;
//            float px = cx + (float) Math.cos(angle) * r;
//            float py = cy + (float) Math.sin(angle) * r;
//            if (i == 0) builder.moveTo(px, py);
//            else builder.lineTo(px, py);
//        }
//        builder.closePath();
//        canvas.drawPath(builder.build(), paint);
//        paint.setAlpha(255);
//    }
//
//    /**
//     * Draws a small 4-point sparkle/cross shape.
//     */
//    private void drawSparkle(Canvas canvas, float x, float y, float size, Paint paint) {
//        PathBuilder builder = new PathBuilder();
//        builder.moveTo(x, y - size);
//        builder.lineTo(x + size * 0.3f, y - size * 0.3f);
//        builder.lineTo(x + size, y);
//        builder.lineTo(x + size * 0.3f, y + size * 0.3f);
//        builder.lineTo(x, y + size);
//        builder.lineTo(x - size * 0.3f, y + size * 0.3f);
//        builder.lineTo(x - size, y);
//        builder.lineTo(x - size * 0.3f, y - size * 0.3f);
//        builder.closePath();
//        canvas.drawPath(builder.build(), paint);
//    }
//
//    // ═══════════════════════════════════════════════════════════
//    //  MINECRAFT ITEM RENDERING
//    // ═══════════════════════════════════════════════════════════
//
//    /**
//     * Renders a Minecraft ItemStack into the Skia canvas at the specified position.
//     * Uses Minecraft's built-in item renderer with proper GL state management.
//     */
//    private void renderItemIcon(ItemStack stack, float x, float y, float size) {
//        if (stack == null) return;
//
//        // Save current GL state
//        GlStateManager.pushMatrix();
//        GlStateManager.pushAttrib();
//
//        RenderHelper.enableGUIStandardItemLighting();
//        GlStateManager.enableRescaleNormal();
//        GlStateManager.enableBlend();
//        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
//
//        // Scale and render
//        float scale = size / 16f;
//        GlStateManager.translate(x, y, 0);
//        GlStateManager.scale(scale, scale, 1f);
//
//        mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
//
//        // Restore GL state
//        GlStateManager.disableRescaleNormal();
//        GlStateManager.disableBlend();
//        RenderHelper.disableStandardItemLighting();
//        GlStateManager.popAttrib();
//        GlStateManager.popMatrix();
//    }
//
//    // ═══════════════════════════════════════════════════════════
//    //  NOTIFICATION DATA CLASS
//    // ═══════════════════════════════════════════════════════════
//
//    private class ItemNotif {
//        final String title;
//        final int count;
//        final ItemStack stack;
//        final Rarity rarity;
//        final long startTime;
//
//        final Animate entryAnim = new Animate(ENTRY_DURATION, Easing.BACK_OUT)
//                .easeIf(() -> true);
//        final Animate exitAnim = new Animate(EXIT_DURATION, Easing.QUART_IN)
//                .easeIf(() -> getTime() >= HOLD_DURATION);
//
//        final List<Particle> particles = new ArrayList<>();
//        boolean particlesInitialized = false;
//
//        ItemNotif(String title, int count, ItemStack stack) {
//            this.title = title;
//            this.count = count;
//            this.stack = stack;
//            this.rarity = Rarity.fromItem(stack);
//            this.startTime = System.currentTimeMillis();
//        }
//
//        long getTime() {
//            return System.currentTimeMillis() - startTime;
//        }
//
//        boolean isExpired() {
//            return getTime() > TOTAL_DURATION;
//        }
//    }
//
//    // ═══════════════════════════════════════════════════════════
//    //  PARTICLE SYSTEM
//    // ═══════════════════════════════════════════════════════════
//
//    private static class Particle {
//        float x, y;
//        float size;
//        float alpha;
//        float speedY;
//        float wobblePhase;
//        float wobbleAmp;
//
//        Particle(float x, float y, float size, float speedY, float wobblePhase, float wobbleAmp) {
//            this.x = x;
//            this.y = y;
//            this.size = size;
//            this.alpha = 1f;
//            this.speedY = speedY;
//            this.wobblePhase = wobblePhase;
//            this.wobbleAmp = wobbleAmp;
//        }
//    }
//
//    private void updateParticles(ItemNotif notif, long notifTime) {
//        // Initialize particles once entry animation settles
//        if (!notif.particlesInitialized && notifTime > ENTRY_DURATION) {
//            notif.particlesInitialized = true;
//            int count = notif.rarity == Rarity.LEGENDARY ? PARTICLE_COUNT_LEG
//                    : notif.rarity == Rarity.EPIC ? PARTICLE_COUNT_EPIC
//                      : PARTICLE_COUNT_RARE;
//
//            for (int i = 0; i < count; i++) {
//                float px = notif.rarity.stars > 0
//                        ? 56 + rng.nextFloat() * 120  // within card bounds
//                        : 56 + rng.nextFloat() * 80;
//                float py = CARD_HEIGHT - 5;
//                float size = 1.5f + rng.nextFloat() * 2f;
//                float speed = PARTICLE_SPEED * (0.6f + rng.nextFloat() * 0.8f);
//                notif.particles.add(new Particle(px, py, size, speed, rng.nextFloat() * 6.28f, 3f + rng.nextFloat() * 5f));
//            }
//        }
//
//        // Update existing particles
//        float dt = 0.016f; // assume 60fps for particle sim
//        Iterator<Particle> it = notif.particles.iterator();
//        while (it.hasNext()) {
//            Particle p = it.next();
//            p.y -= p.speedY * dt;
//            p.x += (float) Math.sin(notifTime * 0.003 + p.wobblePhase) * p.wobbleAmp * dt;
//            p.alpha -= dt * 0.5f; // fade over ~2 seconds
//            if (p.alpha <= 0) {
//                it.remove();
//            }
//        }
//
//        // Respawn particles during hold phase for continuous effect (Legendary only)
//        if (notif.rarity == Rarity.LEGENDARY && notifTime > ENTRY_DURATION && notifTime < HOLD_DURATION
//                && notif.particles.size() < PARTICLE_COUNT_LEG && rng.nextFloat() < 0.1f) {
//            float px = 56 + rng.nextFloat() * 120;
//            float py = CARD_HEIGHT - 5;
//            float size = 1.5f + rng.nextFloat() * 2f;
//            float speed = PARTICLE_SPEED * (0.6f + rng.nextFloat() * 0.8f);
//            notif.particles.add(new Particle(px, py, size, speed, rng.nextFloat() * 6.28f, 3f + rng.nextFloat() * 5f));
//        }
//    }
//}