package eu.shoroa.ross.util.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

import static eu.shoroa.ross.Client.mc;

public class Renderer2D {
    public static void begin2d() {
        ScaledResolution sr = new ScaledResolution(mc);
        int scale = sr.getScaleFactor();

        GlStateManager.pushMatrix();
        GlStateManager.scale(1.0f / scale, 1.0f / scale, 1.0f);
    }

    public static void end2d() {
        GlStateManager.popMatrix();
    }

    public static void drawRect(float x, float y, float width, float height, int color) {
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(red, green, blue, alpha);
        worldRenderer.begin(7, DefaultVertexFormats.POSITION);
        worldRenderer.pos(x, y + height, 0.0F).endVertex();
        worldRenderer.pos(x + width, y + height, 0.0F).endVertex();
        worldRenderer.pos(x + width, y, 0.0F).endVertex();
        worldRenderer.pos(x, y, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void drawBorderedRect(float x, float y, float width, float height, float borderWidth, int color, int borderColor) {
        drawRect(x, y, width, height, color);
        drawRect(x, y, width, borderWidth, borderColor);
        drawRect(x, y + height - borderWidth, width, borderWidth, borderColor);
        drawRect(x, y, borderWidth, height, borderColor);
        drawRect(x + width - borderWidth, y, borderWidth, height, borderColor);
    }

    public static void drawString(String text, float x, float y, int color, boolean shadow) {
        mc.fontRendererObj.drawString(text, (int) x, (int) y, color, shadow);
    }
}
