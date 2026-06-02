package eu.shoroa.ross.mixins.injection.network;

import eu.shoroa.ross.event.EventPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void injectChannelRead0(ChannelHandlerContext channelHandlerContext, Packet packet, CallbackInfo ci) {
        EventPacket.In event = new EventPacket.In(packet);
        EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    public void injectSendPacket(Packet<?> packet, CallbackInfo ci) {
        EventPacket.Out event = new EventPacket.Out(packet);
        EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;[Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
    public void injectSendPacketWithListeners(Packet packetIn, GenericFutureListener<? extends Future<? super Void>> listener, GenericFutureListener<? extends Future<? super Void>>[] listeners, CallbackInfo ci) {
        EventPacket.In event = new EventPacket.In(packetIn);
        EVENT_BUS.post(event);
        if (event.isCanceled()) {
            ci.cancel();
        }
    }
}
