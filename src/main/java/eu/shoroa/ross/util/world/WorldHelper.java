package eu.shoroa.ross.util.world;

import eu.shoroa.ross.types.BlockRef;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static eu.shoroa.ross.Client.mc;

public class WorldHelper {
    public static List<BlockRef> searchBlocks(int radius, Set<Block> targets, Integer maxBlocks, Predicate<BlockRef> predicate) {
        List<BlockRef> blocks = new ArrayList<>();

        if (mc.theWorld == null || mc.thePlayer == null) return blocks;

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(0,0,0);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (maxBlocks != null && blocks.size() >= maxBlocks) {
                        return blocks;
                    }

                    mutable.set((int) (mc.thePlayer.posX + x), (int) (mc.thePlayer.posY + y), (int) (mc.thePlayer.posZ + z));

                    IBlockState state = mc.theWorld.getBlockState(mutable);

                    if (targets == null || state.getBlock() instanceof BlockBed) {
                        BlockRef ref = new BlockRef(new BlockPos(mutable.getX(), mutable.getY(), mutable.getZ()), state);
                        if (predicate == null || predicate.test(ref)) {
                            blocks.add(ref);
                        }
                    }
                }
            }
        }

        return blocks;
    }
}
