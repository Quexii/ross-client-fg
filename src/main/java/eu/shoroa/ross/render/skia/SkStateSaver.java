package eu.shoroa.ross.render.skia;

import org.lwjgl.opengl.*;

public class SkStateSaver {
    private static int lastActiveTexture = 0;
    private static int lastProgram = 0;
    private static int[] lastSamplers = new int[0];
    private static int lastVertexArray = 0;
    private static int lastArrayBuffer = 0;

    private static int lastBlendSrcRgb = 0;
    private static int lastBlendDstRgb = 0;
    private static int lastBlendSrcAlpha = 0;
    private static int lastBlendDstAlpha = 0;
    private static int lastBlendEquationRgb = 0;
    private static int lastBlendEquationAlpha = 0;

    public static void backup() {
        GL11.glPushClientAttrib(GL11.GL_ALL_CLIENT_ATTRIB_BITS);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        lastActiveTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        lastProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int maxUnits = GL11.glGetInteger(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
        if (lastSamplers.length != maxUnits) {
            lastSamplers = new int[maxUnits];
        }
        for (int i = 0; i < maxUnits; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + i);
            lastSamplers[i] = GL11.glGetInteger(GL33.GL_SAMPLER_BINDING);
        }
        GL13.glActiveTexture(lastActiveTexture);
        lastArrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        lastVertexArray = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);

        lastBlendSrcRgb = GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB);
        lastBlendDstRgb = GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        lastBlendSrcAlpha = GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA);
        lastBlendDstAlpha = GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        lastBlendEquationRgb = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_RGB);
        lastBlendEquationAlpha = GL11.glGetInteger(GL20.GL_BLEND_EQUATION_ALPHA);
    }

    public static void restore() {
        GL11.glPopAttrib();
        GL11.glPopClientAttrib();
        GL20.glUseProgram(lastProgram);
        for (int i = 0; i < lastSamplers.length; i++) {
            GL33.glBindSampler(i, lastSamplers[i]);
        }
        GL13.glActiveTexture(lastActiveTexture);
        GL30.glBindVertexArray(lastVertexArray);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, lastArrayBuffer);
        GL20.glBlendEquationSeparate(lastBlendEquationRgb, lastBlendEquationAlpha);
        GL14.glBlendFuncSeparate(lastBlendSrcRgb, lastBlendDstRgb, lastBlendSrcAlpha, lastBlendDstAlpha);
    }
}
