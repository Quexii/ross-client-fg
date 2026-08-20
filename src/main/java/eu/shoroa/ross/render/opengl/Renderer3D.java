package eu.shoroa.ross.render.opengl;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

public class Renderer3D {

    public static void begin3D(float lineWidth) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(lineWidth);
    }

    public static void end3D() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.popMatrix();
    }

    public static void drawLine(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        GlStateManager.color(r, g, b, a);
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        w.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        w.pos(x1, y1, z1).endVertex();
        w.pos(x2, y2, z2).endVertex();
        t.draw();
    }

    public static void drawLine(Vec3 p1, Vec3 p2, int color) {
        drawLine(p1.xCoord, p1.yCoord, p1.zCoord, p2.xCoord, p2.yCoord, p2.zCoord, color);
    }

    public static void drawHitCircle(double x, double y, double z, float radius, EnumFacing face, int r, int g, int b, int a, int segments) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        double ox = offX(face), oy = offY(face), oz = offZ(face);

        w.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < segments; i++) {
            double angle = (2 * Math.PI * i) / segments;
            double[] d = pt(face, angle, radius);
            w.pos(x + ox + d[0], y + oy + d[1], z + oz + d[2]).color(r, g, b, a).endVertex();
        }
        t.draw();
    }

    public static void drawFilledHitCircle(double x, double y, double z, float radius, EnumFacing face, int r, int g, int b, int a, int segments) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        double ox = offX(face), oy = offY(face), oz = offZ(face);

        GlStateManager.disableCull();
        w.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
        w.pos(x + ox, y + oy, z + oz).color(r, g, b, a).endVertex();
        for (int i = 0; i <= segments; i++) {
            double angle = (2 * Math.PI * i) / segments;
            double[] d = pt(face, angle, radius);
            w.pos(x + ox + d[0], y + oy + d[1], z + oz + d[2]).color(r, g, b, a).endVertex();
        }
        t.draw();
        GlStateManager.enableCull();
    }

    public static void drawCross(double x, double y, double z, float size, EnumFacing face, int r, int g, int b, int a) {
        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();
        double ox = offX(face), oy = offY(face), oz = offZ(face);
        float s = size / 2f;

        w.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        switch (face) {
            case UP:
            case DOWN:
                w.pos(x + ox - s, y + oy, z + oz).color(r, g, b, a).endVertex();
                w.pos(x + ox + s, y + oy, z + oz).color(r, g, b, a).endVertex();
                w.pos(x + ox, y + oy, z + oz - s).color(r, g, b, a).endVertex();
                w.pos(x + ox, y + oy, z + oz + s).color(r, g, b, a).endVertex();
                break;
            case NORTH:
            case SOUTH:
                w.pos(x + ox - s, y + oy, z + oz).color(r, g, b, a).endVertex();
                w.pos(x + ox + s, y + oy, z + oz).color(r, g, b, a).endVertex();
                w.pos(x + ox, y + oy - s, z + oz).color(r, g, b, a).endVertex();
                w.pos(x + ox, y + oy + s, z + oz).color(r, g, b, a).endVertex();
                break;
            case EAST:
            case WEST:
                w.pos(x + ox, y + oy, z + oz - s).color(r, g, b, a).endVertex();
                w.pos(x + ox, y + oy, z + oz + s).color(r, g, b, a).endVertex();
                w.pos(x + ox, y + oy - s, z + oz).color(r, g, b, a).endVertex();
                w.pos(x + ox, y + oy + s, z + oz).color(r, g, b, a).endVertex();
                break;
        }
        t.draw();
    }

    public static void drawBoxWirefrae(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();

        GlStateManager.color(r, g, b, a);
        w.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        w.pos(x1, y1, z1).endVertex();
        w.pos(x2, y1, z1).endVertex();
        w.pos(x2, y1, z2).endVertex();
        w.pos(x1, y1, z2).endVertex();
        w.pos(x1, y1, z1).endVertex();
        t.draw();

        w.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
        w.pos(x1, y2, z1).endVertex();
        w.pos(x2, y2, z1).endVertex();
        w.pos(x2, y2, z2).endVertex();
        w.pos(x1, y2, z2).endVertex();
        w.pos(x1, y2, z1).endVertex();
        t.draw();

        w.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        w.pos(x1, y1, z1).endVertex();
        w.pos(x1, y2, z1).endVertex();
        w.pos(x2, y1, z1).endVertex();
        w.pos(x2, y2, z1).endVertex();
        w.pos(x2, y1, z2).endVertex();
        w.pos(x2, y2, z2).endVertex();
        w.pos(x1, y1, z2).endVertex();
        w.pos(x1, y2, z2).endVertex();
        t.draw();
    }

    public static void drawBoxFilled(double x1, double y1, double z1, double x2, double y2, double z2, int color) {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        Tessellator t = Tessellator.getInstance();
        WorldRenderer w = t.getWorldRenderer();

        GlStateManager.color(r, g, b, a);

        for (int pass = 0; pass < 2; pass++) {
            GL11.glCullFace(pass == 0 ? GL11.GL_FRONT : GL11.GL_BACK);

            w.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

            w.pos(x1, y1, z1).endVertex();
            w.pos(x1, y1, z2).endVertex();
            w.pos(x2, y1, z2).endVertex();
            w.pos(x2, y1, z1).endVertex();
            w.pos(x1, y2, z1).endVertex();
            w.pos(x2, y2, z1).endVertex();
            w.pos(x2, y2, z2).endVertex();
            w.pos(x1, y2, z2).endVertex();
            w.pos(x1, y1, z1).endVertex();
            w.pos(x2, y1, z1).endVertex();
            w.pos(x2, y2, z1).endVertex();
            w.pos(x1, y2, z1).endVertex();
            w.pos(x1, y1, z2).endVertex();
            w.pos(x1, y2, z2).endVertex();
            w.pos(x2, y2, z2).endVertex();
            w.pos(x2, y1, z2).endVertex();
            w.pos(x1, y1, z1).endVertex();
            w.pos(x1, y2, z1).endVertex();
            w.pos(x1, y2, z2).endVertex();
            w.pos(x1, y1, z2).endVertex();
            w.pos(x2, y1, z1).endVertex();
            w.pos(x2, y1, z2).endVertex();
            w.pos(x2, y2, z2).endVertex();
            w.pos(x2, y2, z1).endVertex();

            t.draw();
        }

        GL11.glCullFace(GL11.GL_BACK);
    }

    public static void drawBoxWireframe(AxisAlignedBB aabb, int color) {
        drawBoxWirefrae(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, color);
    }

    public static void drawBoxFilled(AxisAlignedBB aabb, int color) {
        drawBoxFilled(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ, color);
    }

    private static double offX(EnumFacing f) {
        return f == EnumFacing.EAST ? 0.003 : f == EnumFacing.WEST ? -0.003 : 0;
    }

    private static double offY(EnumFacing f) {
        return f == EnumFacing.UP ? 0.003 : f == EnumFacing.DOWN ? -0.003 : 0;
    }

    private static double offZ(EnumFacing f) {
        return f == EnumFacing.SOUTH ? 0.003 : f == EnumFacing.NORTH ? -0.003 : 0;
    }

    private static double[] pt(EnumFacing face, double angle, float radius) {
        double c = Math.cos(angle) * radius, s = Math.sin(angle) * radius;
        switch (face) {
            case UP:
            case DOWN:
                return new double[]{c, 0, s};
            case NORTH:
            case SOUTH:
                return new double[]{c, s, 0};
            case EAST:
            case WEST:
                return new double[]{0, s, c};
            default:
                return new double[]{0, 0, 0};
        }
    }
}