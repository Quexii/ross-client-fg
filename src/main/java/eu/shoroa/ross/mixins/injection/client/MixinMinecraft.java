package eu.shoroa.ross.mixins.injection.client;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.render.filters.Filter;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;<init>(Lnet/minecraft/client/Minecraft;)V"))
    public void injectInitGuiIngame(CallbackInfo ci) {
        Client.INSTANCE.init();
    }

    @Inject(method = "resize", at = @At("RETURN"))
    public void injectResize(int width, int height, CallbackInfo ci) {
        if (Client.INSTANCE.skia != null) {
            Client.INSTANCE.skia.resize();
        }

        Filter.kawase().resize();
    }
}
