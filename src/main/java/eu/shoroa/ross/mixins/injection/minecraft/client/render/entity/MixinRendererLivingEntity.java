package eu.shoroa.ross.mixins.injection.minecraft.client.render.entity;

import eu.shoroa.ross.event.EventRenderLiving;
//import eu.shoroa.ross.module.ModuleManager;
//import eu.shoroa.ross.module.impl.render.ModuleESP;
import eu.shoroa.ross.feature.module.ModuleManager;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static eu.shoroa.ross.Client.EVENT_BUS;

@Mixin(RendererLivingEntity.class)
public abstract class MixinRendererLivingEntity {
//    @Inject(method = "canRenderName(Lnet/minecraft/entity/EntityLivingBase;)Z", at = @At("HEAD"), cancellable = true)
//    private void disableNameInESP(EntityLivingBase entity, CallbackInfoReturnable<Boolean> cir) {
//        if (ModuleESP.isShaderPass() && entity instanceof EntityPlayer) {
//            cir.setReturnValue(false);
//        }
//    }

//    @Inject(method = "renderLayers", at = @At("HEAD"), cancellable = true)
//    private void disableLayersInESP(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scaleIn, CallbackInfo ci) {
//        if (ModuleESP.isShaderPass() && entity instanceof EntityPlayer) {
//            ci.cancel();
//        }
//    }

    @Redirect(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isInvisibleToPlayer(Lnet/minecraft/entity/player/EntityPlayer;)Z"))
    public boolean redirectIsInvisibleToPlayer(EntityLivingBase instance, EntityPlayer player) {
        if (ModuleManager.antiInvis.isEnabled() && instance instanceof EntityPlayer) {
            return false;
        }
        return instance.isInvisibleToPlayer(player);
    }

    @ModifyArgs(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;depthMask(Z)V"))
    public void modifyDepthMask(Args args) {
        if (ModuleManager.antiInvis.isEnabled()) {
            args.set(0, true);
        }
    }

    @ModifyArgs(method = "renderModel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;color(FFFF)V"))
    public void modifyColorAlpha(Args args) {
        if (ModuleManager.antiInvis.isEnabled()) {
            float opacity = ModuleManager.antiInvis.getOpacity();
            args.set(3, opacity);
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("HEAD"), cancellable = true)
    private void injectPreRender(EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        EventRenderLiving.Pre event = new EventRenderLiving.Pre(entity, (RendererLivingEntity) (Object) this, x, y, z);
        EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "doRender(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V", at = @At("TAIL"))
    private void injectPostRender(EntityLivingBase entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        EventRenderLiving.Post event = new EventRenderLiving.Post(entity, (RendererLivingEntity) (Object) this, x, y, z);
        EVENT_BUS.post(event);
    }

    @Inject(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At("HEAD"), cancellable = true)
    private void injectPreRenderName(EntityLivingBase entity, double x, double y, double z, CallbackInfo ci) {
        EventRenderLiving.Specials.Pre event = new EventRenderLiving.Specials.Pre(entity, (RendererLivingEntity) (Object) this, x, y, z);
        EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "renderName(Lnet/minecraft/entity/EntityLivingBase;DDD)V", at = @At("TAIL"))
    private void injectPostRenderName(EntityLivingBase entity, double x, double y, double z, CallbackInfo ci) {
        EventRenderLiving.Specials.Post event = new EventRenderLiving.Specials.Post(entity, (RendererLivingEntity) (Object) this, x, y, z);
        EVENT_BUS.post(event);
    }
}