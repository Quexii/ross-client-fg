package eu.shoroa.ross.mixins.injection.client.renderer;

import eu.shoroa.ross.util.proj.Projection;
import net.minecraft.client.renderer.ActiveRenderInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@Mixin(ActiveRenderInfo.class)
public class MixinActiveRenderInfo {
    @Shadow
    @Final
    private static FloatBuffer MODELVIEW;
    @Shadow
    @Final
    private static FloatBuffer PROJECTION;
    @Shadow
    @Final
    private static IntBuffer VIEWPORT;

    @Inject(method = "updateRenderInfo", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGetInteger(ILjava/nio/IntBuffer;)V"))
    private static void updateRenderInfo(CallbackInfo ci) {
        Projection.update(MODELVIEW, PROJECTION, VIEWPORT);
    }
}
