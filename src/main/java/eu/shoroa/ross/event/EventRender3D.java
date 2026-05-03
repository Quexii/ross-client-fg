package eu.shoroa.ross.event;

import net.minecraft.client.renderer.RenderGlobal;

public class EventRender3D {
    public final RenderGlobal context;
    public final float partialTicks;

    public EventRender3D(RenderGlobal context, float partialTicks) {
        this.context = context;
        this.partialTicks = partialTicks;
    }
}
