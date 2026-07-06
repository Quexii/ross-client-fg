package eu.shoroa.ross.mixins.injection.minecraft.client.render.entity;

import eu.shoroa.ross.feature.module.ModuleManager;
import eu.shoroa.ross.feature.module.impl.player.ModuleFreeCam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {
    @Redirect(method = "doRender(Lnet/minecraft/client/entity/AbstractClientPlayer;DDDFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;isUser()Z"))
    private boolean redirectIsUser(AbstractClientPlayer entity) {
        ModuleFreeCam freecam = ModuleManager.freecam;
        if (freecam != null && freecam.isEnabled() && entity == Minecraft.getMinecraft().thePlayer) {
            return false;
        }
        return entity.isUser();
    }
}
