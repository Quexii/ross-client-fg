package eu.shoroa.ross.module.impl.hud;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.animate.Animate;
import eu.shoroa.ross.animate.Easing;
import eu.shoroa.ross.event.EventEntityItemPickup;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.image.Images;
import eu.shoroa.ross.render.skia.shader.Shaders;
import eu.shoroa.ross.util.render.Renderer2D;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Shader;
import io.github.humbleui.types.Rect;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;

import java.util.ArrayDeque;

import static eu.shoroa.ross.Client.mc;

public class ModuleItemNotifs extends Module {
    private final ArrayDeque<ItemNotif> queue = new ArrayDeque<>();

    private final Object queueLock = new Object();

    public ModuleItemNotifs() {
        super("Item Notifier", "Notifies you when you pick up an item.", Category.HUD, null);
    }

    @Subscribe
    //TODO: get a better even hook. this one works only in singleplayer
    public void oe$ItemPickedUp(EventEntityItemPickup event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event.entityPlayer.getEntityId() != mc.thePlayer.getEntityId()) return;

        ItemStack stack = event.item;
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
    }

    @Subscribe
    public void oe$HudVanilla(EventHUD.TopVanilla event) {
        if (queue.isEmpty()) return;

        float notifX = 10f;
        float notifY = 40f;
        float width = 160f;
        float height = 40f;

        float yOffset = 0;
        int max = 5;

        ItemNotif[] snapshot;
        synchronized (queueLock) {
            snapshot = queue.toArray(new ItemNotif[0]);
        }

        Renderer2D.begin2d();
        GlStateManager.scale(2f, 2f, 1f);
        int i = 0;
        for (ItemNotif notif : snapshot) {
            if (i++ >= max) break;

            float x = notifX - width * (float) (1f - (notif.entryAnim.getValue() - notif.exitAnim.getValue()));
            float y = notifY + yOffset;

            renderVanilla(notif, x, y, width, height);

            yOffset += (height + 8) * (notif.entryAnim.getValue() - notif.exitAnim.getValue());
        }
        Renderer2D.end2d();

        synchronized (queueLock) {
            queue.removeIf((n) -> n.isExpired() && n.exitAnim.getValue() >= 0.99);
        }
    }

    private void renderVanilla(ItemNotif notif, float x, float y, float width, float height) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x / 2, y / 2, 0);
        Renderer2D.drawItem(notif.stack, 4f, 2, false);
//        mc.getRenderItem().renderItemAndEffectIntoGUI(notif.stack, 4, 2);
        GlStateManager.popMatrix();
    }

    private void renderSkia(ItemNotif notif, float x, float y, float width, float height, Canvas canvas) {
        int color;

        switch (notif.stack.getRarity()) {
            case COMMON:
                color = 0xFF828282;
                break;
            case UNCOMMON:
                color = 0xFF96FF96;
                break;
            case RARE:
                color = 0xFF9696FF;
                break;
            case EPIC:
                color = 0xFFB428FF;
                break;
            default:
                color = 0xFFFFFFFF;
                break;
        }

        String countText = "x" + notif.count;

        Font titleFont = Fonts.GoogleFlex.weight(400).opticSize(14);
        Font countFont = Fonts.GoogleFlex.weight(600).opticSize(14);

        float textWidth = Renderer.getTextBounds(notif.title, titleFont, 14f).width;

        width = Math.max(width, textWidth + 80f);

        try (Paint p = new Paint()) {
            Renderer.drawFilter(Filter.kawase(), mc.getFramebuffer().framebufferTexture, x, y, width, height, 4);
            p.setColor(0xcf1C1C1C);
            Renderer.drawRRect(x, y, width, height, 4f, p);

            p.setShader(Shader.makeLinearGradient(x, y, x + width * 2/3, y + height, new int[]{Color.makeLerp(color, 0xFF000000, 0.1f), 0x001C1C1C}, null));
            Renderer.drawRect(x + 4f + height, y, width * 2/3, height, p);
            p.setShader(null);

            p.setColor(0x22FFFFFF);
            /*
        seed = 1468280749;
        timeScale = 0.01f;
        freq = 5;
        octave = 2;
        persistence = 0.5f;
        lacunarity = 2.0f;
        color = -1;
        alphaMult = 1f;
        threshold = 0.01f;
        lineWidth = 0.01f;
             */

            Shaders.TOPOGRAPHY.reset();
            Shaders.TOPOGRAPHY
                    .freq(8)
                    .persistence(0.5f)
                    .lacunarity(2.4f)
                    .color(0xFF888888)
                    .alphaMult(0.0f)
            ;
            Shaders.TOPOGRAPHY.update();
            p.setShader(Shaders.TOPOGRAPHY.getShader());
            Renderer.drawRRect(x, y, width, height, 4f, p);
            p.setShader(null);

            p.setColor(color);
            Renderer.drawRRect(x, y, 16f, height, 4f, p);

            p.setColor(Color.makeLerp(color, 0xFF555555, 0.5f));
            Renderer.drawRect(x + 4f, y, height, height, p);

            canvas.drawRectShadowNoclip(Rect.makeXYWH(x + 4 + 8, y + 8, height - 16, height - 16), 0f, 0f, 15f, 0f, 0xBB000000);

            p.setColor(-1);
            Renderer.drawText(notif.title, x + 4 + height + 8, y + height / 2f, titleFont, 14f, Font.Align.CENTER_LEFT, p);
            p.setColor(0xAAFFFFFF);
            Renderer.drawText(countText, x + 4 + height + 8 + 4 + textWidth, y + height / 2f, countFont, 12f, Font.Align.CENTER_LEFT, p);
        }
    }

    private class ItemNotif {
        private final String title;
        private final int count;
        private final ItemStack stack;

        private long startTime;
        private final long duration = 6000;

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
