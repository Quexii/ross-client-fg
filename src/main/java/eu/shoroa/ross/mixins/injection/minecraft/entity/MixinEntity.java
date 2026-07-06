package eu.shoroa.ross.mixins.injection.minecraft.entity;

import eu.shoroa.ross.feature.module.ModuleManager;
import eu.shoroa.ross.feature.module.impl.player.ModuleFreeCam;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow
    public double motionX;
    @Shadow
    public double motionZ;
    @Shadow
    public boolean onGround;
    @Shadow
    public Entity ridingEntity;
    @Shadow
    public float rotationPitch;
    @Shadow
    public float rotationYaw;
    @Shadow
    public double posZ;

    @Shadow
    public abstract AxisAlignedBB getEntityBoundingBox();

    @Shadow
    public double posX;

    @Shadow
    public abstract boolean isSprinting();

    @Inject(method = "setAngles", at = @At("HEAD"), cancellable = true)
    public void onSetAngles(float yaw, float pitch, CallbackInfo ci) {
        ModuleFreeCam freecam = ModuleManager.freecam;
        if (freecam != null && freecam.isEnabled() && (Object) this == Minecraft.getMinecraft().thePlayer) {
            EntityOtherPlayerMP dummy = freecam.getDummy();
            if (dummy != null) {
                dummy.setAngles(yaw, pitch);
                dummy.rotationYawHead = dummy.rotationYaw;
            }
            ci.cancel();
        }
    }
}