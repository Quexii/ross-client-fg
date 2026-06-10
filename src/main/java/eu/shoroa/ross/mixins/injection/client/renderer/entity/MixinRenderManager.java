package eu.shoroa.ross.mixins.injection.client.renderer.entity;

import eu.shoroa.ross.mixins.injection.client.MinecraftAccessor;
import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.module.impl.player.ModuleFreecam;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.mc;
import static org.objectweb.asm.Opcodes.PUTFIELD;

@Mixin(RenderManager.class)
public class MixinRenderManager {
    @Redirect(method = "cacheActiveRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;playerViewX:F", opcode = PUTFIELD))
    public void getPlayerViewX(RenderManager renderManager, float value) {
        ModuleFreecam freecam = ModuleManager.freecam;
        if (freecam.isEnabled()) {
            renderManager.playerViewX = freecam.getCamPitch();
        } else {
            renderManager.playerViewX = ModuleManager.freeLook.isEnabled() ? ModuleManager.freeLook.getPitch() : value;
        }
    }

    @Redirect(method = "cacheActiveRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;playerViewY:F", opcode = PUTFIELD))
    public void getPlayerViewY(RenderManager renderManager, float value) {
        ModuleFreecam freecam = ModuleManager.freecam;
        if (freecam.isEnabled()) {
            renderManager.playerViewY = freecam.getCamYaw();
        } else {
            renderManager.playerViewY = ModuleManager.freeLook.isEnabled() ? ModuleManager.freeLook.getYaw() : value;
        }
    }

    @Redirect(method = "cacheActiveRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;viewerPosX:D", opcode = PUTFIELD))
    public void setViewerPosX(RenderManager renderManager, double value) {
        ModuleFreecam freecam = ModuleManager.freecam;
        if (freecam.isEnabled()) {
            float partialTicks = ((MinecraftAccessor)mc).getTimer().renderPartialTicks;
            renderManager.viewerPosX = ModuleManager.freecam.getRenderX(partialTicks);
        } else {
            renderManager.viewerPosX = value;
        }
    }

    @Redirect(method = "cacheActiveRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;viewerPosY:D", opcode = PUTFIELD))
    public void setViewerPosY(RenderManager renderManager, double value) {
        ModuleFreecam freecam = ModuleManager.freecam;
        if (freecam.isEnabled()) {
            float partialTicks = ((MinecraftAccessor)mc).getTimer().renderPartialTicks;
            renderManager.viewerPosY = ModuleManager.freecam.getRenderY(partialTicks);
        } else {
            renderManager.viewerPosY = value;
        }
    }

    @Redirect(method = "cacheActiveRenderInfo", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/entity/RenderManager;viewerPosZ:D", opcode = PUTFIELD))
    public void setViewerPosZ(RenderManager renderManager, double value) {
        ModuleFreecam freecam = ModuleManager.freecam;
        if (freecam.isEnabled()) {
            float partialTicks = ((MinecraftAccessor)mc).getTimer().renderPartialTicks;
            renderManager.viewerPosZ = ModuleManager.freecam.getRenderZ(partialTicks);
        } else {
            renderManager.viewerPosZ = value;
        }
    }
}