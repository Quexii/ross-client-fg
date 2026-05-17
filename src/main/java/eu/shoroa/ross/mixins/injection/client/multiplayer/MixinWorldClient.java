package eu.shoroa.ross.mixins.injection.client.multiplayer;

import eu.shoroa.ross.event.EventWorld;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;

@Mixin(WorldClient.class)
public class MixinWorldClient {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectInit(NetHandlerPlayClient p_i45063_1_, WorldSettings p_i45063_2_, int p_i45063_3_, EnumDifficulty p_i45063_4_, Profiler p_i45063_5_, CallbackInfo ci) {
        EVENT_BUS.post(new EventWorld.Init((WorldClient) (Object) this));
    }

//    @Redirect(method = "doPreChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ChunkProviderClient;loadChunk(II)Lnet/minecraft/world/chunk/Chunk;"))
//    private Chunk redirectLoadChunk(ChunkProviderClient instance, int chunkX, int chunkZ) {
//        Chunk chunk = instance.loadChunk(chunkX, chunkZ);
//        EVENT_BUS.post(new EventWorld.LoadChunk((WorldClient) (Object) this, chunk));
//        return chunk;
//    }

    @Redirect(method = "doPreChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ChunkProviderClient;unloadChunk(II)V"))
    private void redirectUnloadChunk(ChunkProviderClient instance, int chunkX, int chunkZ) {
        EVENT_BUS.post(new EventWorld.UnloadChunk((WorldClient) (Object) this, chunkX, chunkZ));
    }
}
