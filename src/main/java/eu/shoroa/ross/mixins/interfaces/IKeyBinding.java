package eu.shoroa.ross.mixins.interfaces;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

public interface IKeyBinding {
    void setPressed(boolean pressed);
}