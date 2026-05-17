package eu.shoroa.ross.event;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class EventChunk {
    public final Chunk chunk;

    public EventChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public static class OnBlockState extends EventChunk {
        public final BlockPos pos;
        public final IBlockState state;

        public OnBlockState(Chunk chunk, BlockPos pos, IBlockState state) {
            super(chunk);
            this.pos = pos;
            this.state = state;
        }
    }
}
