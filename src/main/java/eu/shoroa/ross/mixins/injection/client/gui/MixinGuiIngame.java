package eu.shoroa.ross.mixins.injection.client.gui;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;
import static eu.shoroa.ross.Client.mc;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "renderGameOverlay", at = @At(value = "HEAD"))
    public void preGameOverlay(float partialTicks, CallbackInfo ci) {
        ScaledResolution sr = new ScaledResolution(mc);

        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        try {
            GlStateManager.clear(256);
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0F, sr.getScaledWidth_double(), sr.getScaledHeight_double(), 0.0F, 1000.0F, 3000.0F);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0F, 0.0F, -2000.0F);

            EVENT_BUS.post(new EventHUD.PreHud.Vanilla(partialTicks));
            if (Client.INSTANCE.skia != null && Client.INSTANCE.skia.getCanvas() != null) {
                Client.INSTANCE.skia.beginFrame();
                Renderer.use(Client.INSTANCE.skia);
                EVENT_BUS.post(new EventHUD.PreHud.Skia(partialTicks));
                Client.INSTANCE.skia.endFrame();
            }
        } finally {
            GlStateManager.matrixMode(5888);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5889);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
        }
    }

    @Inject(method = "renderTooltip", at = @At(value = "HEAD"))
    public void renderTooltip(ScaledResolution sr, float partialTicks, CallbackInfo ci) {
        Filter.kawase().capture(mc.getFramebuffer().framebufferTexture, 4f, true, 4);

        EVENT_BUS.post(new EventHUD.BottomVanilla(partialTicks));
        if (Client.INSTANCE.skia != null && Client.INSTANCE.skia.getCanvas() != null) {
            Client.INSTANCE.skia.beginFrame();
            Renderer.use(Client.INSTANCE.skia);
            EVENT_BUS.post(new EventHUD.BottomSkia(partialTicks));
            Client.INSTANCE.skia.endFrame();
        }

        EVENT_BUS.post(new EventHUD.TopVanilla(partialTicks));
        if (Client.INSTANCE.skia != null && Client.INSTANCE.skia.getCanvas() != null) {
            Client.INSTANCE.skia.beginFrame();
            Renderer.use(Client.INSTANCE.skia);
            EVENT_BUS.post(new EventHUD.TopSkia(partialTicks));
            Client.INSTANCE.skia.endFrame();
        }
    }
}
