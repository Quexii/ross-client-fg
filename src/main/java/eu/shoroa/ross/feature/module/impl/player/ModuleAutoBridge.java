package eu.shoroa.ross.feature.module.impl.player;

import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.mixins.interfaces.IKeyBinding;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import org.jetbrains.annotations.ApiStatus;

import static eu.shoroa.ross.Client.mc;

public class ModuleAutoBridge extends Module {
//    private final SettingCategory settings = addCategory("Settings", "settings", "settings");

    public ModuleAutoBridge() {
        super("AutoBridge", "Automatically places blocks to bridge gaps", Category.PLAYER, MaterialIcons.BLOCK);
    }

    @Subscribe
    @ApiStatus.Internal
    public void onUpdate(EventTick event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        ItemStack heldItem = mc.thePlayer.getHeldItem();

        if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
//            releaseUse();
            return;
        }
        if (mc.thePlayer.rotationPitch < 60) {
//            releaseUse();
            return;
        }

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
//            releaseUse();
            return;
        }

        EnumFacing facing = mop.sideHit;
        BlockPos hitPos = mop.getBlockPos();

        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
//            releaseUse();
            return;
        }

        mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, heldItem, hitPos, facing, mc.objectMouseOver.hitVec);
        mc.thePlayer.swingItem();
//        ((IKeyBinding) mc.gameSettings.keyBindUseItem).setPressed(true);
    }

    private void releaseUse() {
        ((IKeyBinding) mc.gameSettings.keyBindUseItem).setPressed(false);
    }
}