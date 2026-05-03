package eu.shoroa.ross.render.gl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.Map;

import static eu.shoroa.ross.Client.mc;

public class ItemBaker {
    private static final ItemBaker INSTANCE = new ItemBaker();

    public static ItemBaker getInstance() {
        return INSTANCE;
    }

    Map<String, BakedItem> cache = new HashMap<>();

    public BakedItem getOrCreate(ItemStack stack) {
        String key = getKey(stack);

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        Framebuffer fbo = new Framebuffer(64, 64, true); // 16 * 4
        fbo.setFramebufferFilter(GL11.GL_LINEAR); // important

        fbo.bindFramebuffer(true);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GlStateManager.pushMatrix();
        GlStateManager.pushAttrib();

        // Scale up 4×
        GlStateManager.scale(4f, 4f, 1f);

        GlStateManager.enableRescaleNormal();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        RenderHelper.enableGUIStandardItemLighting();

        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);

        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();

        fbo.unbindFramebuffer();

        BakedItem ci = new BakedItem();
        ci.fb = fbo;

        cache.put(key, ci);

        return ci;
    }

    String getKey(ItemStack stack) {
        return stack.getItem().getUnlocalizedName() + ":" + stack.getMetadata();
    }

    private ItemBaker() {
    }

    public class BakedItem {
        public Framebuffer fb;
    }
}
