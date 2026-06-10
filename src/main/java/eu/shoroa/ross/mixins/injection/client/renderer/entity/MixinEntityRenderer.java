package eu.shoroa.ross.mixins.injection.client.renderer.entity;

import eu.shoroa.ross.event.EventRender3D;
import eu.shoroa.ross.mixins.injection.client.MinecraftAccessor;
import eu.shoroa.ross.module.ModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

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

    @ModifyArgs(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/culling/ICamera;setPosition(DDD)V"))
    public void modifyCameraSetPosition(Args args) {
        if (ModuleManager.freecam.isEnabled()) {
            float partialTicks = ((MinecraftAccessor) mc).getTimer().renderPartialTicks;

            double x = ModuleManager.freecam.getPrevCamX() + (ModuleManager.freecam.getCamX() - ModuleManager.freecam.getPrevCamX()) * (double)partialTicks;
            double y = ModuleManager.freecam.getPrevCamY() + (ModuleManager.freecam.getCamY() - ModuleManager.freecam.getPrevCamY()) * (double)partialTicks;
            double z = ModuleManager.freecam.getPrevCamZ() + (ModuleManager.freecam.getCamZ() - ModuleManager.freecam.getPrevCamZ()) * (double)partialTicks;

            args.set(0, x);
            args.set(1, y);
            args.set(2, z);
        }
    }

    @Redirect(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;orientCamera(F)V", opcode = GETFIELD))
    public void setupCameraTransform(EntityRenderer instance, float partialTicks) {
        if (ModuleManager.freecam.isEnabled()) {
            ModuleManager.freecam.orientCamera(partialTicks);
        } else {
            instance.orientCamera(partialTicks);
        }
    }

    @Redirect(method = "updateCameraAndRender", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;inGameHasFocus:Z", opcode = GETFIELD))
    public boolean updateCameraAndRender(Minecraft minecraft) {
        if (ModuleManager.freecam.isEnabled()) return ModuleManager.freecam.applyMouseDelta();
        return ModuleManager.freeLook.applyMouseDelta();
    }
}
