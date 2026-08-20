package eu.shoroa.ross.type;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

public class BlockRef {
    public final BlockPos pos;
    public final IBlockState state;

    public BlockRef(BlockPos pos, IBlockState state) {
        this.pos = pos;
        this.state = state;
    }
}