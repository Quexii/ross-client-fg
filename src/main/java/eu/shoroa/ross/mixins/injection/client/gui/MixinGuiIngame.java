package eu.shoroa.ross.mixins.injection.client.gui;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.EventInGameHUD;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.filters.Filter;
import eu.shoroa.ross.render.skia.shader.Shaders;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;
import static eu.shoroa.ross.Client.mc;

@Mixin(GuiIngame.class)
public abstract class MixinGuiIngame {

    @Shadow
    protected abstract void renderPlayerStats(ScaledResolution scaledRes);

    @Shadow
    protected abstract void renderTooltip(ScaledResolution sr, float partialTicks);

    @Shadow
    public abstract void renderExpBar(ScaledResolution scaledRes, int x);

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

    @Inject(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderTooltip(Lnet/minecraft/client/gui/ScaledResolution;F)V"))
    public void renderTooltip(float partialTicks, CallbackInfo ci) {
        Shaders.update();

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

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderPlayerStats(Lnet/minecraft/client/gui/ScaledResolution;)V"))
    public void redirectRenderPlayerStats(GuiIngame instance, ScaledResolution scaledRes) {
        EventInGameHUD.Stats event = new EventInGameHUD.Stats();
        EVENT_BUS.post(event);
        if (!event.isCanceled()) this.renderPlayerStats(scaledRes);
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderTooltip(Lnet/minecraft/client/gui/ScaledResolution;F)V"))
    public void redirectRenderTooltip(GuiIngame instance, ScaledResolution sr, float partialTicks) {
        EventInGameHUD.Hotbar event = new EventInGameHUD.Hotbar();
        EVENT_BUS.post(event);
        if (!event.isCanceled()) this.renderTooltip(sr, partialTicks);
    }

    @Redirect(method = "renderGameOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;renderExpBar(Lnet/minecraft/client/gui/ScaledResolution;I)V"))
    public void redirectRenderExperienceBar(GuiIngame instance, ScaledResolution scaledRes, int x) {
        EventInGameHUD.XP event = new EventInGameHUD.XP();
        EVENT_BUS.post(event);
        if (!event.isCanceled()) this.renderExpBar(scaledRes, x);
    }
}
