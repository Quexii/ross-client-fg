package eu.shoroa.ross.event;

import net.minecraft.world.World;

public class EventWorld {
    public final World world;

    public EventWorld(World world) {
        this.world = world;
    }

    public static class Load extends EventWorld {
        public Load(World world) {
            super(world);
        }
    }
}
