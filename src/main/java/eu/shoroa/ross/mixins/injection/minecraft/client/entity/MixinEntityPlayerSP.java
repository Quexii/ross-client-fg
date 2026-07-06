package eu.shoroa.ross.mixins.injection.minecraft.client.entity;

import eu.shoroa.ross.event.EventMotion;
import eu.shoroa.ross.event.EventSelfUpdate;
import eu.shoroa.ross.feature.command.CommandManager;
import eu.shoroa.ross.feature.module.ModuleManager;
import eu.shoroa.ross.mixins.interfaces.IMinecraft;
import eu.shoroa.ross.mixins.injection.minecraft.entity.MixinEntityLivingBase;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static eu.shoroa.ross.Client.EVENT_BUS;
import static eu.shoroa.ross.Client.mc;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends MixinEntityLivingBase {
    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    private boolean serverSprintState;

    @Shadow
    @Final
    public NetHandlerPlayClient sendQueue;

    @Shadow
    private boolean serverSneakState;

    @Shadow
    protected abstract boolean isCurrentViewEntity();

    @Shadow
    private double lastReportedPosX;

    @Shadow
    private double lastReportedPosY;

    @Shadow
    private double lastReportedPosZ;

    @Shadow
    private float lastReportedYaw;

    @Shadow
    private float lastReportedPitch;

    @Shadow
    private int positionUpdateTicks;

    @Inject(method = "isCurrentViewEntity", at = @At("HEAD"), cancellable = true)
    private void forceCurrentViewEntity(CallbackInfoReturnable<Boolean> cir) {
        if (ModuleManager.freecam != null && ModuleManager.freecam.isEnabled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChat(String message, CallbackInfo ci) {
        if (message.startsWith(".")) {
            CommandManager.handleCommands(message.substring(1));
            ci.cancel();
        }
    }

    @Inject(method = "onUpdate", at = @At("HEAD"))
    private void injectUpdate(CallbackInfo ci) {
        EVENT_BUS.post(new EventSelfUpdate());
    }

    @Unique
    public int airTicks = 0;

    /**
     * @author shoroa
     * @reason Hooking motion events
     */
    @Overwrite
    public void onUpdateWalkingPlayer() {
        EntityPlayerSP player = (EntityPlayerSP) (Object) this;
        EventMotion.Pre preMotionEvent = new EventMotion.Pre(player, this.posX, this.getEntityBoundingBox().minY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround, ((IMinecraft) mc).getTimer().renderPartialTicks);
        EVENT_BUS.post(preMotionEvent);

        boolean isSprinting = this.isSprinting();

        if (isSprinting != this.serverSprintState) {
            if (isSprinting) {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SPRINTING));
            } else {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }

            this.serverSprintState = isSprinting;
        }

        boolean isSneaking = this.isSneaking();

        if (isSneaking != this.serverSneakState) {
            if (isSneaking) {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(player, C0BPacketEntityAction.Action.START_SNEAKING));
            } else {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(player, C0BPacketEntityAction.Action.STOP_SNEAKING));
            }

            this.serverSneakState = isSneaking;
        }

        if (this.isCurrentViewEntity()) {
            if (!preMotionEvent.isCanceled()) {
                double posX = preMotionEvent.x, posY = preMotionEvent.y, posZ = preMotionEvent.z;
                float rotationYaw = preMotionEvent.yaw, rotationPitch = preMotionEvent.pitch;
                boolean onGround = preMotionEvent.onGround;

                if (onGround)
                    airTicks = 0;
                else
                    airTicks++;

                double d0 = posX - this.lastReportedPosX;
                double d1 = posY - this.lastReportedPosY;
                double d2 = posZ - this.lastReportedPosZ;
                double d3 = rotationYaw - this.lastReportedYaw;
                double d4 = rotationPitch - this.lastReportedPitch;
                boolean moving = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || this.positionUpdateTicks >= 20;
                boolean movingHead = d3 != 0.0D || d4 != 0.0D;

                if (this.ridingEntity == null) {
                    if (moving && movingHead) {
                        this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(posX, posY, posZ, rotationYaw, rotationPitch, onGround));
                    } else if (moving) {
                        this.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(posX, posY, posZ, onGround));
                    } else if (movingHead) {
                        this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(rotationYaw, rotationPitch, onGround));
                    } else {
                        this.sendQueue.addToSendQueue(new C03PacketPlayer(onGround));
                    }
                } else {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(this.motionX, -999.0D, this.motionZ, rotationYaw, rotationPitch, onGround));
                    moving = false;
                }

                ++this.positionUpdateTicks;

                if (moving) {
                    this.lastReportedPosX = posX;
                    this.lastReportedPosY = posY;
                    this.lastReportedPosZ = posZ;
                    this.positionUpdateTicks = 0;
                }

                if (movingHead) {
                    this.lastReportedYaw = rotationYaw;
                    this.lastReportedPitch = rotationPitch;
                }
            }
            EventMotion.Post postMotionEvent = new EventMotion.Post(player, ((IMinecraft) mc).getTimer().renderPartialTicks);
            EVENT_BUS.post(postMotionEvent);
        }
    }
}