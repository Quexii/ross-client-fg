package eu.shoroa.ross.mixins.injection.world.chunk;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventChunk;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Chunk.class)
public class MixinChunk {
    @Inject(method = "setBlockState", at = @At("RETURN"))
    private void injectSetBlockState(BlockPos pos, IBlockState state, CallbackInfoReturnable<IBlockState> cir) {
        Client.EVENT_BUS.post(new EventChunk.OnBlockState((Chunk) (Object) this, pos, state));
    }
}