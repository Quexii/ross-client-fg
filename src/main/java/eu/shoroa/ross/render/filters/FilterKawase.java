package eu.shoroa.ross.render.filters;

import eu.shoroa.ross.render.gl.Shader;
import eu.shoroa.ross.render.gl.uniform.Uniform;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import static eu.shoroa.ross.Client.mc;

public class FilterKawase extends Filter {
    private Framebuffer[] fbos = new Framebuffer[0];
    private int currentIterations = 0;

    public FilterKawase() {
        super(new Shader("shaders/vertex.vert", "shaders/kawase.frag"));
    }

    private void allocateFbos(int iterations) {
        for (Framebuffer fbo : fbos) fbo.deleteFramebuffer();
        fbos = new Framebuffer[iterations];
        int w = mc.displayWidth, h = mc.displayHeight;
        for (int i = 0; i < iterations; i++) {
            int fw = Math.max(w >> (i + 1), 1);
            int fh = Math.max(h >> (i + 1), 1);
            fbos[i] = new Framebuffer(fw, fh, false);
            fbos[i].setFramebufferFilter(GL11.GL_LINEAR);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos[i].framebufferTexture);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        currentIterations = iterations;
    }

    @Override
    protected void createFbos() {
        if (currentIterations > 0) allocateFbos(currentIterations);
    }

    @Override
    public void capture(int texture, Object... args) {
        if (args.length != 3 || !(args[0] instanceof Float) || !(args[1] instanceof Boolean) || !(args[2] instanceof Integer))
            throw new IllegalArgumentException("Invalid arguments, expected (float radius, boolean ignoreAlpha, int iterations)");
        capture(texture, (float) args[0], (boolean) args[1], (int) args[2]);
    }

    public void capture(int texture, float radius, boolean ignoreAlpha, int iterations) {
        int maxMeaningful = radius >= 1f ? (int)(Math.log(radius) / Math.log(2)) + 1 : 1;
        int n = Math.max(1, Math.min(iterations, maxMeaningful));
        if (n != currentIterations) allocateFbos(n);

        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        ScaledResolution sr = new ScaledResolution(mc);
        int w = mc.displayWidth, h = mc.displayHeight;

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        for (int i = 0; i < n; i++) {
            int srcTex = (i == 0) ? texture : fbos[i - 1].framebufferTexture;
            float srcW = w / (float)(1 << i);
            float srcH = h / (float)(1 << i);
            float r = 0.5f * radius / (float)(1 << (n - 1 - i));

            fbos[i].framebufferClear();
            fbos[i].bindFramebuffer(true);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, srcTex);
            shader.attach();
            shader.uniform(Uniform.makeInt("uTex", 0));
            shader.uniform(Uniform.makeInt("doAlpha", ignoreAlpha ? 1 : 0));
            shader.uniform(Uniform.makeVec2("uResolution", srcW, srcH));
            shader.uniform(Uniform.makeFloat("uRadius", r));
            shader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());
            shader.detach();
        }

        for (int i = n - 1; i >= 1; i--) {
            float srcW = w / (float)(1 << (i + 1));
            float srcH = h / (float)(1 << (i + 1));
            float r = 0.5f * radius / (float)(1 << (n - 1 - i));

            fbos[i - 1].framebufferClear();
            fbos[i - 1].bindFramebuffer(true);
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            fbos[i].bindFramebufferTexture();
            shader.attach();
            shader.uniform(Uniform.makeInt("uTex", 0));
            shader.uniform(Uniform.makeInt("doAlpha", ignoreAlpha ? 1 : 0));
            shader.uniform(Uniform.makeVec2("uResolution", srcW, srcH));
            shader.uniform(Uniform.makeFloat("uRadius", r));
            shader.rect(0f, 0f, sr.getScaledWidth(), sr.getScaledHeight());
            shader.detach();
        }

        mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1f, 1f, 1f, 1f);

        putImage(texture, fbos[0].framebufferTexture, fbos[0].framebufferTextureWidth, fbos[0].framebufferTextureHeight);
    }
}
