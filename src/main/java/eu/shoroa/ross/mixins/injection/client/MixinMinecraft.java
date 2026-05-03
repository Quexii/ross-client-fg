package eu.shoroa.ross.mixins.injection.client;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.mixins.interfaces.IEntityLivingBase;
import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.render.filters.Filter;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;

@Mixin(value = Minecraft.class, priority = 1100)
public class MixinMinecraft {
    @Shadow
    public GameSettings gameSettings;

    @Shadow
    private int leftClickCounter;

    @Shadow
    public WorldClient theWorld;

    @Shadow
    public EffectRenderer effectRenderer;

    @Shadow
    public EntityPlayerSP thePlayer;

    @Shadow
    public MovingObjectPosition objectMouseOver;

    @Shadow
    public PlayerControllerMP playerController;

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiIngame;<init>(Lnet/minecraft/client/Minecraft;)V"))
    public void injectInitGuiIngame(CallbackInfo ci) {
        Client.INSTANCE.init();
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 0))
    public void injectRunTick(CallbackInfo ci) {
        EVENT_BUS.post(new EventTick());
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", ordinal = 0))
    public void injectHandleKeyboard(CallbackInfo ci) {
        EVENT_BUS.post(new EventInput(Keyboard.getEventKey(), EventInput.Type.KEYBOARD, Keyboard.getEventKeyState() ? EventInput.Action.PRESS : EventInput.Action.RELEASE));
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z", shift = At.Shift.AFTER))
    public void injectHandleMouse(CallbackInfo ci) {
        EVENT_BUS.post(new EventInput(Mouse.getEventButton(), EventInput.Type.MOUSE, Mouse.getEventButtonState() ? EventInput.Action.PRESS : EventInput.Action.RELEASE));
    }

    @Inject(method = "resize", at = @At("RETURN"))
    public void injectResize(int width, int height, CallbackInfo ci) {
        if (Client.INSTANCE.skia != null) {
            Client.INSTANCE.skia.resize();
        }
        Filter.kawase().resize();
    }

    @ModifyConstant(method = "getLimitFramerate", constant = @Constant(intValue = 30), require = 0)
    public int modifyGetLimitFramerate(int original) {
        return 144;
    }

    @Inject(method = "sendClickBlockToController", at = @At("HEAD"), cancellable = true)
    public void blockHitting(boolean leftClick, CallbackInfo ci) {
        if (ModuleManager.animations.isEnabled()) {
            ci.cancel();
            doBlockHitting(leftClick);
        }
    }

    private void doBlockHitting(boolean leftClick) {
        if (gameSettings.keyBindUseItem.isKeyDown()) {
            if (leftClickCounter <= 0 && leftClick && objectMouseOver != null && objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (!theWorld.isAirBlock(objectMouseOver.getBlockPos()) && thePlayer.isAllowEdit()) {

                    effectRenderer.addBlockHitEffects(objectMouseOver.getBlockPos(), objectMouseOver.sideHit);

                    if (!thePlayer.isSwingInProgress || thePlayer.swingProgressInt >= (((IEntityLivingBase) thePlayer).getArmSwingAnimEnd()) / 2 || thePlayer.swingProgressInt < 0) {
                        thePlayer.swingProgressInt = -1;
                        thePlayer.isSwingInProgress = true;
                    }
                }
            } else {
                playerController.resetBlockRemoving();
            }
        }

        if (!leftClick) {
            this.leftClickCounter = 0;
        }

        if (this.leftClickCounter <= 0 && !this.thePlayer.isUsingItem()) {
            if (leftClick && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                BlockPos blockpos = this.objectMouseOver.getBlockPos();

                if (this.theWorld.getBlockState(blockpos).getBlock().getMaterial() != Material.air && this.playerController.onPlayerDamageBlock(blockpos, this.objectMouseOver.sideHit)) {
                    this.effectRenderer.addBlockHitEffects(blockpos, this.objectMouseOver.sideHit);
                    this.thePlayer.swingItem();
                }
            } else {
                this.playerController.resetBlockRemoving();
            }
        }
    }
}
