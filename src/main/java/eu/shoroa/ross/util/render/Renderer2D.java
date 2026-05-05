package eu.shoroa.ross.util.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;

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

    public static void drawTexturedRect(float x, float y, float width, float height, float u, float v, float uWidth, float vHeight) {
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldRenderer.pos(x, y + height, 0.0D).tex(u / 256f, (v + vHeight) / 256f).endVertex();
        worldRenderer.pos(x + width, y + height, 0.0D).tex((u + uWidth) / 256f, (v + vHeight) / 256f).endVertex();
        worldRenderer.pos(x + width, y, 0.0D).tex((u + uWidth) / 256f, v / 256f).endVertex();
        worldRenderer.pos(x, y, 0.0D).tex(u / 256f, v / 256f).endVertex();
        tessellator.draw();
    }

    public static void drawItem(ItemStack stack, float x, float y, boolean mipmaps) {
        RenderItem ri = mc.getRenderItem();

        IBakedModel model = ri.getItemModelMesher().getItemModel(stack);
        GlStateManager.pushMatrix();

        GlStateManager.enableRescaleNormal();
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).setBlurMipmap(mipmaps, mipmaps);
        GlStateManager.enableRescaleNormal();
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.translate(x, y, 100.0F + ri.zLevel);
        GlStateManager.translate(8.0F, 8.0F, 0.0F);
        GlStateManager.scale(1.0F, 1.0F, -1.0F);
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        if (model.isGui3d()) {
            GlStateManager.scale(40.0F, 40.0F, 40.0F);
            GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.enableLighting();
        } else {
            GlStateManager.scale(64.0F, 64.0F, 64.0F);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.disableLighting();
        }
        model.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);
        ri.renderItem(stack, model);
        GlStateManager.disableAlpha();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        GlStateManager.popMatrix();
        mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        mc.getTextureManager().getTexture(TextureMap.locationBlocksTexture).restoreLastBlurMipmap();
    }

    public static void drawString(String text, float x, float y, int color, boolean shadow) {
        mc.fontRendererObj.drawString(text, (int) x, (int) y, color, shadow);
    }
}
