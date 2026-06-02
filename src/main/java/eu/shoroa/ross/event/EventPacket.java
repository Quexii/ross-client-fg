package eu.shoroa.ross.event;

import net.minecraft.network.Packet;

public class EventPacket implements Cancelable {
    private boolean cancelled = false;

    public final Packet<?> packet;

    public EventPacket(Packet<?> packet) {
        this.packet = packet;
    }

    @Override
    public boolean isCanceled() {
        return cancelled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.cancelled = canceled;
    }

    public static class In extends EventPacket {
        public In(Packet<?> packet) {
            super(packet);
        }
    }

    public static class Out extends EventPacket {
        public Out(Packet<?> packet) {
            super(packet);
        }
    }
}
