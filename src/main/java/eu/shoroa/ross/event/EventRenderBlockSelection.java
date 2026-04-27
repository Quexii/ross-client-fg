package eu.shoroa.ross.event;

import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EventRenderBlockSelection extends Event {
    private final AxisAlignedBB boundingBox;

    public EventRenderBlockSelection(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }
}
