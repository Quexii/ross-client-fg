package eu.shoroa.ross.mixins.injection.minecraft.util;

import eu.shoroa.ross.feature.module.ModuleManager;
import eu.shoroa.ross.feature.module.impl.player.ModuleFreeCam;
import net.minecraft.util.MovementInput;
import net.minecraft.util.MovementInputFromOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MovementInputFromOptions.class)
public class MixinMovementInputFromOptions {
    @Inject(method = "updatePlayerMoveState", at = @At("HEAD"), cancellable = true)
    public void onUpdatePlayerMoveState(CallbackInfo ci) {
        ModuleFreeCam freecam = ModuleManager.freecam;
        if (freecam != null && freecam.isEnabled()) {
            MovementInput input = (MovementInput) (Object) this;
            input.moveForward = 0.0F;
            input.moveStrafe = 0.0F;
            input.jump = false;
            input.sneak = freecam.getSneak();
            ci.cancel();
        }
    }
}
