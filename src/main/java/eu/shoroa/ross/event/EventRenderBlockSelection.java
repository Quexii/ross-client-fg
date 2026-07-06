package eu.shoroa.ross.event;

import eu.shoroa.ross.event.api.Cancelable;
import net.minecraft.util.AxisAlignedBB;

public class EventRenderBlockSelection implements Cancelable {
    private boolean cancelled;

    private final AxisAlignedBB boundingBox;

    public EventRenderBlockSelection(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
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