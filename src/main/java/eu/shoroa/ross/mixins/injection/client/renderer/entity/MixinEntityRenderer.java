package eu.shoroa.ross.mixins.injection.client.renderer.entity;

import eu.shoroa.ross.event.EventRender3D;
import eu.shoroa.ross.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;
import static org.objectweb.asm.Opcodes.GETFIELD;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Shadow
    private Minecraft mc;

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = "ldc=hand"))
    public void injectEvent3D(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
        EVENT_BUS.post(new EventRender3D(mc.renderGlobal, partialTicks));
    }

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
}
