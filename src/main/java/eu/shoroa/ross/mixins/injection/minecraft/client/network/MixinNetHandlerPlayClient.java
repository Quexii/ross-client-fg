package eu.shoroa.ross.mixins.injection.minecraft.client.network;

import eu.shoroa.ross.event.EventChatReceived;
import eu.shoroa.ross.event.EventEntityItemPickup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Shadow
    private Minecraft gameController;

    @Shadow
    private WorldClient clientWorldController;

    @Inject(method = "handleChat", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER), cancellable = true)
    private void injectOnChat(S02PacketChat packetIn, CallbackInfo ci) {
        EventChatReceived event = new EventChatReceived(packetIn.getChatComponent(), packetIn.getType());
        EVENT_BUS.post(event);

        if (event.isCanceled()) {
            ci.cancel();
        }

        if (!packetIn.getChatComponent().equals(event.message) || packetIn.getType() != event.type) {
            if (event.message != null) {
                if (packetIn.getType() == 2) {
                    this.gameController.ingameGUI.setRecordPlaying(event.message, false);
                } else {
                    this.gameController.ingameGUI.getChatGUI().printChatMessage(event.message);
                }
            }
            ci.cancel();
        }
    }

    @Unique
    private EntityPlayer pickedUpBy;

    @Inject(method = "handleCollectItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketThreadUtil;checkThreadAndEnqueue(Lnet/minecraft/network/Packet;Lnet/minecraft/network/INetHandler;Lnet/minecraft/util/IThreadListener;)V", shift = At.Shift.AFTER))
    private void injectOnCollectItem(S0DPacketCollectItem packetIn, CallbackInfo ci) {
        EntityLivingBase pickedBy = (EntityLivingBase) this.clientWorldController.getEntityByID(packetIn.getEntityID());

        pickedUpBy = null;

        if (pickedBy == null) {
            pickedBy = this.gameController.thePlayer;
        }

        if (pickedBy instanceof EntityPlayer) {
            pickedUpBy = (EntityPlayer) pickedBy;
        }
    }

    @Redirect(method = "handleCollectItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;playSoundAtEntity(Lnet/minecraft/entity/Entity;Ljava/lang/String;FF)V", ordinal = 1))
    private void onItemPickup(WorldClient instance, Entity entity, String s, float volume, float pitch) {
        if (entity instanceof EntityItem) {
            EntityItem entityitem = (EntityItem) entity;
            EVENT_BUS.post(new EventEntityItemPickup(pickedUpBy, entityitem.getEntityItem()));
        }
    }
}