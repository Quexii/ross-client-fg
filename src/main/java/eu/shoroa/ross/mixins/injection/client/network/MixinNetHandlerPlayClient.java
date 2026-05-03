package eu.shoroa.ross.mixins.injection.client.network;

import eu.shoroa.ross.event.EventChatReceived;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Shadow
    private Minecraft gameController;

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
}
