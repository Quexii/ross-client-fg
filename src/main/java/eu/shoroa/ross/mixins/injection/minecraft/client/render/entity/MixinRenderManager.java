package eu.shoroa.ross.mixins.injection.minecraft.client.render.entity;

import eu.shoroa.ross.event.EventDoRenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static eu.shoroa.ross.Client.EVENT_BUS;

@Mixin(RenderManager.class)
public class MixinRenderManager {
    @Inject(method = "doRenderEntity", at = @At("HEAD"))
    private void onPreDoRenderEntity(Entity entity, double x, double y, double z, float entityYaw, float partialTicks, boolean p_147939_10_, CallbackInfoReturnable<Boolean> cir) {
        EVENT_BUS.post(new EventDoRenderEntity.Pre(entity, x, y, z, entityYaw, partialTicks));
    }

    @Inject(method = "doRenderEntity", at = @At("RETURN"))
    private void onPostDoRenderEntity(Entity entity, double x, double y, double z, float entityYaw, float partialTicks, boolean p_147939_10_, CallbackInfoReturnable<Boolean> cir) {
        EVENT_BUS.post(new EventDoRenderEntity.Post(entity, x, y, z, entityYaw, partialTicks));
    }
}
