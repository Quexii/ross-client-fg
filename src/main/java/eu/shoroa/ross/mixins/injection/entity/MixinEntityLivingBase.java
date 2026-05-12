package eu.shoroa.ross.mixins.injection.entity;

import eu.shoroa.ross.event.EventLiving;
import eu.shoroa.ross.mixins.interfaces.IEntityLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends MixinEntity implements IEntityLivingBase {
    @Shadow
    protected abstract int getArmSwingAnimationEnd();

    @Override
    public int getArmSwingAnimEnd() {
        return this.getArmSwingAnimationEnd();
    }

    @Inject(method = "damageEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;isEntityInvulnerable(Lnet/minecraft/util/DamageSource;)Z", shift = At.Shift.AFTER))
    public void injectOnDamage(DamageSource damageSrc, float damageAmount, CallbackInfo ci) {
        EVENT_BUS.post(new EventLiving.Damage((EntityLivingBase) (Object) this, damageAmount, damageSrc));
    }

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
    public void injectOnLivingUpdate(CallbackInfo ci) {
        EVENT_BUS.post(new EventLiving.Update((EntityLivingBase) (Object) this));
    }

    @Inject(method = "jump", at = @At("TAIL"))
    public void injectOnJump(CallbackInfo ci) {
        EVENT_BUS.post(new EventLiving.Jump((EntityLivingBase) (Object) this));
    }
}
