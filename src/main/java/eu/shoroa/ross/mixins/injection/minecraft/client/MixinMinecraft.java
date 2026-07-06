package eu.shoroa.ross.mixins.injection.minecraft.client;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.LifeCycle;
import eu.shoroa.ross.feature.module.ModuleManager;
import eu.shoroa.ross.mixins.interfaces.IEntityLivingBase;
import eu.shoroa.ross.mixins.interfaces.IMinecraft;
import eu.shoroa.ross.render.animate.Animate;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Timer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IMinecraft {
    @Shadow
    public static long getSystemTime() {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private Timer timer;
    @Shadow
    private int leftClickCounter;
    @Shadow
    private int rightClickDelayTimer;
    @Shadow
    public GameSettings gameSettings;
    @Shadow
    public WorldClient theWorld;
    @Shadow
    public MovingObjectPosition objectMouseOver;
    @Shadow
    public EntityPlayerSP thePlayer;
    @Shadow
    public EffectRenderer effectRenderer;
    @Shadow
    public PlayerControllerMP playerController;
    @Unique
    private long lastTime = getSystemTime();

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/GuiIngameForge;<init>(Lnet/minecraft/client/Minecraft;)V"))
    public void startClient(CallbackInfo ci) {
        Client.INSTANCE.EVENT_BUS.post(new LifeCycle.Start());
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdownClient(CallbackInfo ci) {
        Client.INSTANCE.EVENT_BUS.post(new LifeCycle.Stop());
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;startSection(Ljava/lang/String;)V", ordinal = 0))
    public void injectRunTick(CallbackInfo ci) {
        Client.INSTANCE.EVENT_BUS.post(new EventTick());
    }

    @Inject(method = "resize", at = @At("TAIL"))
    public void resizeClient(int width, int height, CallbackInfo ci) {
        Client.INSTANCE.EVENT_BUS.post(new LifeCycle.Resize(width, height));
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;getEventKey()I", ordinal = 0))
    public void injectHandleKeyboard(CallbackInfo ci) {
        Client.INSTANCE.EVENT_BUS.post(new EventInput(Keyboard.getEventKey(), EventInput.Type.KEYBOARD, Keyboard.getEventKeyState() ? EventInput.Action.PRESS : EventInput.Action.RELEASE));
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z", shift = At.Shift.AFTER))
    public void injectHandleMouse(CallbackInfo ci) {
        Client.INSTANCE.EVENT_BUS.post(new EventInput(Mouse.getEventButton(), EventInput.Type.MOUSE, Mouse.getEventButtonState() ? EventInput.Action.PRESS : EventInput.Action.RELEASE));
    }

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    public void injectUpdateDelta(CallbackInfo ci) {
        long currentTime = getSystemTime();
        long deltaTime = currentTime - lastTime;
        lastTime = currentTime;

        Animate.DELTA_TIME = deltaTime;
    }

    @Inject(method = "clickMouse", at = @At("HEAD"), cancellable = true)
    public void onClickMouse(CallbackInfo ci) {
        if (isFreecamActive()) ci.cancel();
    }

    @Inject(method = "rightClickMouse", at = @At("HEAD"), cancellable = true)
    public void onRightClickMouse(CallbackInfo ci) {
        if (isFreecamActive()) ci.cancel();
    }

    private boolean isFreecamActive() {
        return ModuleManager.freecam != null && ModuleManager.freecam.isEnabled();
    }

    @Inject(method = "sendClickBlockToController", at = @At("HEAD"), cancellable = true)
    public void blockHitting(boolean leftClick, CallbackInfo ci) {
        if (isFreecamActive()) {
            ci.cancel();
            return;
        }
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

    @Override
    public Timer getTimer() {
        return this.timer;
    }

    @Override
    public void setLeftClickCounter(int leftClickCounter) {
        this.leftClickCounter = leftClickCounter;
    }

    @Override
    public void setRightClickDelayTimer(int rightClickDelayTimer) {
        this.rightClickDelayTimer = rightClickDelayTimer;
    }
}
