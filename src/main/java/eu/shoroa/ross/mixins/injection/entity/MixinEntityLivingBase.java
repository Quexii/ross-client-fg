package eu.shoroa.ross.mixins.injection.entity;

import eu.shoroa.ross.mixins.interfaces.IEntityLivingBase;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase implements IEntityLivingBase {
    @Shadow
    protected abstract int getArmSwingAnimationEnd();

    @Override
    public int getArmSwingAnimEnd() {
        return this.getArmSwingAnimationEnd();
    }
}
