package eu.shoroa.ross.render.skia;

import io.github.humbleui.skija.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class Skia {
    private final Framebuffer framebuffer;
    private static DirectContext context;
    private BackendRenderTarget renderTarget;
    private Surface surface;
    private int previousFramebuffer = -1;

    public Skia(Framebuffer framebuffer) {
        this.framebuffer = framebuffer;
    }

    public void init() {
        if (context == null) context = DirectContext.makeGL();

        renderTarget = BackendRenderTarget.makeGL(
            framebuffer.framebufferTextureWidth,
            framebuffer.framebufferTextureHeight,
            0,
            8,
            framebuffer.framebufferObject,
            GL11.GL_RGBA8
        );

        surface = Surface.wrapBackendRenderTarget(context, renderTarget, SurfaceOrigin.BOTTOM_LEFT, ColorType.RGBA_8888, ColorSpace.getSRGB());
    }

    public void destroy() {
        if (surface != null) {
            surface.close();
        }

        if (renderTarget != null) {
            renderTarget.close();
        }
    }

    public void resize() {
        destroy();
        init();
    }

    public void beginFrame() {
        previousFramebuffer = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

        if (framebuffer.framebufferObject != Minecraft.getMinecraft().getFramebuffer().framebufferObject) {
            framebuffer.framebufferClear();
        }

        GlStateManager.pushAttrib();
        States.save();
        context.resetGLAll();
        GlStateManager.disableAlpha();
    }

    public void endFrame() {
        context.flushAndSubmit(surface);
        GlStateManager.enableAlpha();
        States.restore();
        GlStateManager.popAttrib();

        if (previousFramebuffer != -1) {
            GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFramebuffer);
        }
    }

    public boolean isStale() {
        return surface == null || surface.isClosed() || renderTarget == null || renderTarget.isClosed() || (getCanvas() != null && getCanvas().isClosed());
    }

    @Nullable
    public Canvas getCanvas() {
        return surface != null ? surface.getCanvas() : null;
    }

    @Nullable
    public Surface getSurface() {
        return surface;
    }

    public DirectContext getContext() {
        return context;
    }
}