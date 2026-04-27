package eu.shoroa.ross.mixins.injection.client.renderer.entity;

import eu.shoroa.ross.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.objectweb.asm.Opcodes.GETFIELD;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Shadow
    private Minecraft mc;

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationYaw:F", opcode = GETFIELD))
    public float getRotationYaw(Entity entity) {
        return ModuleManager.freeLook.isEnabled() ? ModuleManager.freeLook.getYaw() : entity.rotationYaw;
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationYaw:F", opcode = GETFIELD))
    public float getPrevRotationYaw(Entity entity) {
        return ModuleManager.freeLook.isEnabled() ? ModuleManager.freeLook.getYaw() : entity.prevRotationYaw;
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationPitch:F", opcode = GETFIELD))
    public float getRotationPitch(Entity entity) {
        return ModuleManager.freeLook.isEnabled() ? ModuleManager.freeLook.getPitch() : entity.rotationPitch;
    }

    @Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationPitch:F"))
    public float getPrevRotationPitch(Entity entity) {
        return ModuleManager.freeLook.isEnabled() ? ModuleManager.freeLook.getPitch() : entity.prevRotationPitch;
    }

    @Redirect(method = "updateCameraAndRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;inGameHasFocus:Z", opcode = GETFIELD))
    public boolean updateCameraAndRender(Minecraft minecraft) {
        return ModuleManager.freeLook.applyMouseDelta();
    }

//    @Redirect(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setAngles(FF)V", ordinal = 0))
//    public void redirectSetAnglesSmooth(EntityPlayerSP instance, float yaw, float pitch) {
//        boolean freeLookActive = ModuleManager.freeLook.applyMouseDelta(mc.mouseHelper.deltaX, mc.mouseHelper.deltaY);
//
//        if (freeLookActive) return;
//        mc.thePlayer.setAngles(yaw, pitch);
//    }
//
//    @Redirect(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setAngles(FF)V", ordinal = 0))
//    public void redirectSetAngles(EntityPlayerSP instance, float yaw, float pitch) {
//        boolean freeLookActive = ModuleManager.freeLook.applyMouseDelta(mc.mouseHelper.deltaX, mc.mouseHelper.deltaY);
//
//        if (freeLookActive) return;
//        mc.thePlayer.setAngles(yaw, pitch);
//    }

//    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/Frustum;<init>()V"))
//    public void disableCulling(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
//        if (ModuleManager.freeLook.isEnabled()) ClippingHelperImpl.getInstance().
//    }
}
