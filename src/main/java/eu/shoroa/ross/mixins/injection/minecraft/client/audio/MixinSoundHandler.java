package eu.shoroa.ross.mixins.injection.minecraft.client.audio;

import eu.shoroa.ross.mixins.interfaces.ISoundHandler;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SoundHandler.class)
public class MixinSoundHandler implements ISoundHandler {
    @Shadow
    @Final
    public SoundManager sndManager;

    @Override
    public SoundManager getSoundManager() {
        return sndManager;
    }
}
