package eu.shoroa.ross.mixins.injection.client.renderer;

import eu.shoroa.ross.event.EventRenderBlockSelection;
import eu.shoroa.ross.event.EventRenderEntities;
import eu.shoroa.ross.module.ModuleManager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.shoroa.ross.Client.EVENT_BUS;

@Mixin(value = RenderGlobal.class, priority = 1100)
public class MixinRenderGlobal {
    @Shadow
    private WorldClient theWorld;

    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "drawSelectionBox", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/AxisAlignedBB;expand(DDD)Lnet/minecraft/util/AxisAlignedBB;", shift = At.Shift.BEFORE), cancellable = true, require = 0)
    public void injectSelectionEvent(EntityPlayer player, MovingObjectPosition movingObjectPositionIn, int execute, float partialTicks, CallbackInfo ci) {
        BlockPos blockpos = movingObjectPositionIn.getBlockPos();
        Block block = this.theWorld.getBlockState(blockpos).getBlock();
        double d0 = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) partialTicks;
        double d1 = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) partialTicks;
        double d2 = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) partialTicks;

        AxisAlignedBB aabb = block.getSelectedBoundingBox(this.theWorld, blockpos).expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D).offset(-d0, -d1, -d2);
        EventRenderBlockSelection event = new EventRenderBlockSelection(aabb);
        EVENT_BUS.post(event);

        if (event.isCanceled()) ci.cancel();
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.BEFORE))
    public void injectRenderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        EVENT_BUS.post(new EventRenderEntities.Post(partialTicks, camera, renderViewEntity, theWorld.getLoadedEntityList()));
    }

//    @Redirect(method = "setupTerrain", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;posX:D"))
//    public double redirectSetupX(Entity entity) {
//        if (ModuleManager.freecam.isEnabled()) {
//            return ModuleManager.freecam.getCamX();
//        }
//        return entity.posX;
//    }
//
//    @Redirect(method = "setupTerrain", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;posY:D"))
//    public double redirectSetupY(Entity entity) {
//        if (ModuleManager.freecam.isEnabled()) {
//            return ModuleManager.freecam.getCamY();
//        }
//        return entity.posY;
//    }
//
//    @Redirect(method = "setupTerrain", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;posZ:D"))
//    private double redirectSetupZ(Entity entity) {
//        if (ModuleManager.freecam.isEnabled()) {
//            return ModuleManager.freecam.getCamZ();
//        }
//        return entity.posZ;
//    }
//
//    @Redirect(method = "setupTerrain", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;lastTickPosX:D"))
//    public double redirectSetupPrevX(Entity entity) {
//        if (ModuleManager.freecam.isEnabled()) {
//            return ModuleManager.freecam.getPrevCamX();
//        }
//        return entity.lastTickPosX;
//    }
//
//    @Redirect(method = "setupTerrain", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;lastTickPosY:D"))
//    public double redirectSetupPrevY(Entity entity) {
//        if (ModuleManager.freecam.isEnabled()) {
//            return ModuleManager.freecam.getPrevCamY();
//        }
//        return entity.lastTickPosY;
//    }
//
//    @Redirect(method = "setupTerrain", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;lastTickPosZ:D"))
//    private double redirectSetupPrevZ(Entity entity) {
//        if (ModuleManager.freecam.isEnabled()) {
//            return ModuleManager.freecam.getPrevCamZ();
//        }
//        return entity.lastTickPosZ;
//    }

    @Redirect(method = "setupTerrain", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationYaw:F"))
    public float redirectSetupYaw(Entity entity) {
        if (ModuleManager.freecam.isEnabled()) {
            return ModuleManager.freecam.getCamYaw();
        }
        return entity.rotationYaw;
    }

    @Redirect(method = "setupTerrain", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationPitch:F"))
    private float redirectSetupPitch(Entity entity) {
        if (ModuleManager.freecam.isEnabled()) {
            return ModuleManager.freecam.getCamPitch();
        }
        return entity.rotationPitch;
    }
}
