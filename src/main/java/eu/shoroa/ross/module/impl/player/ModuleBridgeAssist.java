package eu.shoroa.ross.module.impl.player;

import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.mixins.injection.client.settings.KeyBindingAccessor;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.settings.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;

import static eu.shoroa.ross.Client.mc;

public class ModuleBridgeAssist extends Module {
    private final NumberSetting edgeOffset = register(
            new NumberSetting("Edge Offset", 0.15f, 0.01f, 0.7f, 0.01f));

    private final NumberSetting unsneakDelay = register(
            new NumberSetting("Unsneak Delay", 3f, 0f, 20f, 1f));

    private final BooleanSetting randomize = register(
            new BooleanSetting("Randomize", false));

    private final BooleanSetting requireSneak = register(
            new BooleanSetting("Sneak Key Pressed", false));

    private final BooleanSetting requireHoldingBlocks = register(
            new BooleanSetting("Holding Blocks", true));

    private final BooleanSetting requireLookDown = register(
            new BooleanSetting("Looking Down", true));

    private final BooleanSetting requireNotMovingForward = register(
            new BooleanSetting("Not Moving Forward", true));

    private final Random rng = new Random();
    private float currentEdgeOffset;
    private float currentUnsneakDelay;
    private int unsneakTicks = 0;
    private boolean wasSneaking = false;

    public ModuleBridgeAssist() {
        super("Bridge Assist", "Automatically shifts when on edge", Category.PLAYER, null);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        refreshRandomizedValues();
        unsneakTicks = 0;
        wasSneaking = false;
    }

    private void refreshRandomizedValues() {
        if (randomize.get()) {
            float baseOffset = edgeOffset.get();
            currentEdgeOffset = baseOffset * (0.8f + rng.nextFloat() * 0.4f);

            float baseDelay = unsneakDelay.get();
            currentUnsneakDelay = baseDelay * (0.8f + rng.nextFloat() * 0.4f);
        } else {
            currentEdgeOffset = edgeOffset.get();
            currentUnsneakDelay = unsneakDelay.get();
        }
    }

    @SubscribeEvent
    public void oe$OnUpdate(EventTick event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (!mc.thePlayer.onGround) {
            if (wasSneaking) {
                ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(true);
            }
            return;
        }

        boolean conditionsMet = true;
        if (requireSneak.get() && !GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) conditionsMet = false;
        if (requireHoldingBlocks.get() && !isHoldingBlocks()) conditionsMet = false;
        if (requireLookDown.get() && mc.thePlayer.rotationPitch < 45f) conditionsMet = false;
        if (requireNotMovingForward.get() && GameSettings.isKeyDown(mc.gameSettings.keyBindForward))
            conditionsMet = false;

        boolean onEdge = conditionsMet && isNearEdge(currentEdgeOffset);

        if (onEdge) {
            if (!wasSneaking) {
                refreshRandomizedValues();
            }
            wasSneaking = true;
            unsneakTicks = 0;
            ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(true);
        } else {
            if (wasSneaking) {
                unsneakTicks++;
                if (unsneakTicks >= (int) currentUnsneakDelay) {
                    wasSneaking = false;
                    unsneakTicks = 0;
                    ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(GameSettings.isKeyDown(mc.gameSettings.keyBindSneak));
                } else {
                    ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(true);
                }
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.gameSettings != null) {
            ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(false);
        }
        wasSneaking = false;
        unsneakTicks = 0;
    }

    private boolean isNearEdge(float offset) {
        AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox();
        if (bb == null) return false;

        double maxInsetX = ((bb.maxX - bb.minX) / 2.0) - 1.0E-3;
        double maxInsetZ = ((bb.maxZ - bb.minZ) / 2.0) - 1.0E-3;
        double insetX = Math.max(0.0, Math.min(offset, maxInsetX));
        double insetZ = Math.max(0.0, Math.min(offset, maxInsetZ));

        double minX = bb.minX + insetX;
        double maxX = bb.maxX - insetX;
        double minZ = bb.minZ + insetZ;
        double maxZ = bb.maxZ - insetZ;
        double midX = (minX + maxX) * 0.5;
        double midZ = (minZ + maxZ) * 0.5;
        double probeY = bb.minY - 0.5;

        double[][] probes = {
                {minX, probeY, minZ},
                {maxX, probeY, minZ},
                {minX, probeY, maxZ},
                {maxX, probeY, maxZ},
                {midX, probeY, minZ},
                {midX, probeY, maxZ},
                {minX, probeY, midZ},
                {maxX, probeY, midZ}
        };

        for (double[] probe : probes) {
            BlockPos below = new BlockPos(probe[0], probe[1], probe[2]);
            Material mat = mc.theWorld.getBlockState(below).getBlock().getMaterial();
            if (mat == Material.air) {
                return true;
            }
        }
        return false;
    }

    private boolean isHoldingBlocks() {
        ItemStack held = mc.thePlayer.inventory.getCurrentItem();
        return held != null && held.getItem() instanceof ItemBlock;
    }
}
