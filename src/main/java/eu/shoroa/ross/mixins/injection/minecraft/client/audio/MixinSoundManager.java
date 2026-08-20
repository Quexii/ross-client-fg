package eu.shoroa.ross.mixins.injection.minecraft.client.audio;

import eu.shoroa.ross.mixins.interfaces.ISoundManager;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import paulscode.sound.SoundSystem;

import java.lang.reflect.Field;

@Mixin(SoundManager.class)
public class MixinSoundManager implements ISoundManager {

    @Unique
    private static Field ross$sndSystemField;

    @Override
    public SoundSystem getSoundSystem() {
        try {
            if (ross$sndSystemField == null) {
                ross$sndSystemField = ross$findSndSystemField();
            }
            return (SoundSystem) ross$sndSystemField.get(this);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    @Unique
    private static Field ross$findSndSystemField() throws NoSuchFieldException {
        NoSuchFieldException last = null;
        for (String name : new String[]{"sndSystem", "field_148620_e"}) {
            try {
                Field f = SoundManager.class.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                last = e;
            }
        }
        throw last;
    }
}
