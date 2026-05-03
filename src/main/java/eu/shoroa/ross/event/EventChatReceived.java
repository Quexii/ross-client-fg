package eu.shoroa.ross.event;

import net.minecraft.util.IChatComponent;

public class EventChatReceived implements Cancelable {
    private boolean cancelled;
    public final IChatComponent message;
    public final byte type;

    public EventChatReceived(IChatComponent message, byte type) {
        this.message = message;
        this.type = type;
    }


    @Override
    public boolean isCanceled() {
        return cancelled;
    }

    @Override
    public void setCanceled(boolean canceled) {
        this.cancelled = canceled;
    }
}
