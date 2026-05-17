package eu.shoroa.ross.event;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class EventWorld {
    public final World world;

    public EventWorld(World world) {
        this.world = world;
    }

    public static class Init extends EventWorld {
        public Init(World world) {
            super(world);
        }
    }

    public static class LoadChunk extends EventWorld {
        public final Chunk chunk;
        public LoadChunk(World world, Chunk chunk) {
            super(world);
            this.chunk = chunk;
        }
    }

    public static class UnloadChunk extends EventWorld {
        public final int x, z;
        public UnloadChunk(World world, int x, int z) {
            super(world);
            this.x = x;
            this.z = z;
        }
    }
}
