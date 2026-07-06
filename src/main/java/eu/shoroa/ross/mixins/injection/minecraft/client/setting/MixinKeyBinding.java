package eu.shoroa.ross.mixins.injection.minecraft.client.setting;

import eu.shoroa.ross.mixins.interfaces.IKeyBinding;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyBinding.class)
public class MixinKeyBinding implements IKeyBinding {
    @Shadow
    private boolean pressed;

    @Override
    public void setPressed(boolean b) {
        pressed = b;
    }
}