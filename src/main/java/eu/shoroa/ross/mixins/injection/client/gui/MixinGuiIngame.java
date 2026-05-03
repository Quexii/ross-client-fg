package eu.shoroa.ross.mixins.injection.client.gui;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;
import static eu.shoroa.ross.Client.mc;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;setupOverlayRendering()V", shift = At.Shift.AFTER))
    public void preGameOverlay(float partialTicks, CallbackInfo ci) {
        EVENT_BUS.post(new EventHUD.PreHud.Vanilla(partialTicks));
        if (Client.INSTANCE.skia != null && Client.INSTANCE.skia.getCanvas() != null) {
            Client.INSTANCE.skia.beginFrame();
            Renderer.use(Client.INSTANCE.skia);
            EVENT_BUS.post(new EventHUD.PreHud.Skia(partialTicks));
            Client.INSTANCE.skia.endFrame();
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
