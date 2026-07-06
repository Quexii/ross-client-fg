package eu.shoroa.ross.mixins.injection.minecraft.client.render;

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

    @Shadow
    private static float rotationX;

    @Shadow
    private static float rotationZ;

    @Shadow
    private static float rotationYZ;

    @Shadow
    private static float rotationXY;

    @Shadow
    private static float rotationXZ;

    @Inject(method = "updateRenderInfo", at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glGetInteger(ILjava/nio/IntBuffer;)V"))
    private static void updateRenderInfo(CallbackInfo ci) {
        eu.shoroa.ross.utils.proj.Projection.update(MODELVIEW, PROJECTION, VIEWPORT);
    }

//    @Inject(method = "updateRenderInfo", at = @At("TAIL"))
//    private static void updateRenderInfoXZ(EntityPlayer entityplayerIn, boolean bl, CallbackInfo ci) {
//        if (!ModuleManager.freecam.isEnabled()) return;
//
//        int i = 0;
//
//        float h = ModuleManager.freecam.getCamPitch();
//        float j = ModuleManager.freecam.getCamYaw();
//        rotationX = MathHelper.cos(j * (float)Math.PI / 180.0F) * (float)(1 - i * 2);
//        rotationZ = MathHelper.sin(j * (float)Math.PI / 180.0F) * (float)(1 - i * 2);
//        rotationYZ = -rotationZ * MathHelper.sin(h * (float)Math.PI / 180.0F) * (float)(1 - i * 2);
//        rotationXY = rotationX * MathHelper.sin(h * (float)Math.PI / 180.0F) * (float)(1 - i * 2);
//        rotationXZ = MathHelper.cos(h * (float)Math.PI / 180.0F);
//    }
}