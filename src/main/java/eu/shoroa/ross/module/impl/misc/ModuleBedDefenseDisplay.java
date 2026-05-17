package eu.shoroa.ross.module.impl.misc;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.*;
import eu.shoroa.ross.mixins.injection.client.renderer.entity.RenderManagerAccessor;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.types.BlockRef;
import eu.shoroa.ross.util.proj.Projection;
import eu.shoroa.ross.util.render.MaterialIcons;
import eu.shoroa.ross.util.render.Renderer2D;
import eu.shoroa.ross.util.render.Renderer3D;
import eu.shoroa.ross.util.world.WorldHelper;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Color;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.types.Rect;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.Chunk;

import java.util.*;

import static eu.shoroa.ross.Client.mc;

public class ModuleBedDefenseDisplay extends Module {
    private List<BlockRef> blocks = new ArrayList<>();
    private Map<BlockPos, List<ItemStack>> defenseBlocks = new HashMap<>();
    private Map<BlockPos, Vec3> screenPositions = new HashMap<>();

    private static final Set<Block> targets = new HashSet<>(Arrays.asList(Blocks.bed));
    private static final int DEFENSE_RADIUS = 1;
    private static final int DEFENSE_MAX_Y_OFFSET = 3;
    private static final int[][] CARDINAL_DIRS = new int[][]{
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };
    private static final int COLOR_RED = 0xFFEF3939;
    private static final int COLOR_YELLOW = 0xFFFCF22F;
    private static final int COLOR_GREEN = 0xFF84EF37;
    private static final float RED_STRENGTH = 1.0f;
    private static final float GREEN_STRENGTH = 4.0f;

    private Thread scanThread;
    private volatile boolean scanRunning;

    public ModuleBedDefenseDisplay() {
        super("Bed Defense Overlay", "Displays the blocks around beds.", Category.MISC);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        scanRunning = true;
        scanThread = new Thread(() -> {
            while (isEnabled() && scanRunning) {
                blocks = WorldHelper.searchBlocks(80, targets, 100, (blockRef -> {
                    BlockBed bed = (BlockBed) blockRef.state.getBlock();
                    return blockRef.state.getProperties().get(BlockBed.PART).equals(BlockBed.EnumPartType.HEAD);
                }));
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException ignored) {
                    break;
                }
            }
        }, "bed-defense-scan");
        scanThread.setDaemon(true);
        scanThread.start();
    }

    @Override
    public void onDisable() {
        scanRunning = false;
        if (scanThread != null) {
            scanThread.interrupt();
            scanThread = null;
        }
        super.onDisable();
    }

    @Subscribe
    public void oe$Render3D(EventRender3D event) {
        float partialTicks = event.partialTicks;
        double renderX = ((RenderManagerAccessor) mc.getRenderManager()).getRenderPosX();
        double renderY = ((RenderManagerAccessor) mc.getRenderManager()).getRenderPosY();
        double renderZ = ((RenderManagerAccessor) mc.getRenderManager()).getRenderPosZ();

        screenPositions.clear();
        defenseBlocks.clear();
        if (mc.theWorld == null) return;
        for (BlockRef ref : blocks) {
            BlockPos pos = ref.pos;
            float addX = 0f;
            float addZ = 0f;
            if (mc.theWorld.getBlockState(pos.add(1, 0, 0)).getBlock() instanceof BlockBed) addX += 0.5f;
            if (mc.theWorld.getBlockState(pos.add(-1, 0, 0)).getBlock() instanceof BlockBed) addX -= 0.5f;
            if (mc.theWorld.getBlockState(pos.add(0, 0, 1)).getBlock() instanceof BlockBed) addZ += 0.5f;
            if (mc.theWorld.getBlockState(pos.add(0, 0, -1)).getBlock() instanceof BlockBed) addZ -= 0.5f;

            List<ItemStack> defenseLayers = collectDefenseLayers(pos);
            if (!defenseLayers.isEmpty()) {
                defenseBlocks.put(pos, defenseLayers);
            }


            Vec3 projected = Projection.w2s(
                    (float) (pos.getX() + 0.5f - renderX + addX),
                    (float) (pos.getY() + 0.5f - renderY),
                    (float) (pos.getZ() + 0.5f - renderZ + addZ)
            );
            screenPositions.put(pos, projected);
        }
    }

    private List<ItemStack> collectDefenseLayers(BlockPos bedPos) {
        Set<BlockPos> bedParts = new HashSet<>();
        bedParts.add(bedPos);
        for (int[] dir : CARDINAL_DIRS) {
            BlockPos neighbor = bedPos.add(dir[0], 0, dir[1]);
            if (mc.theWorld.getBlockState(neighbor).getBlock() instanceof BlockBed) {
                bedParts.add(neighbor);
            }
        }

        List<ItemStack> layers = new ArrayList<>();
        for (int yOffset = 0; yOffset <= DEFENSE_MAX_Y_OFFSET; yOffset++) {
            if (bedPos.getY() + yOffset < 0) {
                continue;
            }

            boolean hasDirectAirPath = false;
            for (BlockPos part : bedParts) {
                for (int[] dir : CARDINAL_DIRS) {
                    BlockPos checkPos = part.add(dir[0], yOffset, dir[1]);
                    if (checkPos.getY() < 0) {
                        continue;
                    }
                    if (mc.theWorld.isAirBlock(checkPos)) {
                        hasDirectAirPath = true;
                        break;
                    }
                }
                if (hasDirectAirPath) {
                    break;
                }
            }

            if (hasDirectAirPath) {
                continue;
            }

            Set<BlockPos> ringPositions = new HashSet<>();
            for (BlockPos part : bedParts) {
                for (int dx = -DEFENSE_RADIUS; dx <= DEFENSE_RADIUS; dx++) {
                    for (int dz = -DEFENSE_RADIUS; dz <= DEFENSE_RADIUS; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        BlockPos checkPos = part.add(dx, yOffset, dz);
                        if (checkPos.getY() < 0) continue;
                        ringPositions.add(checkPos);
                    }
                }
            }

            Map<Integer, Integer> counts = new HashMap<>();
            Map<Integer, ItemStack> samples = new HashMap<>();
            for (BlockPos checkPos : ringPositions) {
                if (mc.theWorld.isAirBlock(checkPos)) {
                    continue;
                }
                Block block = mc.theWorld.getBlockState(checkPos).getBlock();
                if (block instanceof BlockBed) continue;
                int meta = block.getMetaFromState(mc.theWorld.getBlockState(checkPos));
                int key = (Block.getIdFromBlock(block) << 4) | (meta & 0xF);
                counts.put(key, counts.getOrDefault(key, 0) + 1);
                samples.putIfAbsent(key, new ItemStack(block, 1, meta));
            }

            int dominantKey = -1;
            int maxCount = 0;
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    dominantKey = entry.getKey();
                    maxCount = entry.getValue();
                }
            }
            if (dominantKey != -1) {
                ItemStack dominant = samples.get(dominantKey);
                if (dominant != null) {
                    layers.add(dominant);
                }
            }
        }

        return layers;
    }

    private int getDefenseStateColor(List<ItemStack> layers, ItemStack[] inventory) {
        int worstScore = 2;
        for (ItemStack itemBlock : layers) {
            Block block = Block.getBlockFromItem(itemBlock.getItem());
            if (block == null) {
                continue;
            }

            float bestStrength = RED_STRENGTH;
            for (ItemStack stack : inventory) {
                if (stack == null) continue;
                float strength = stack.getStrVsBlock(block);
                if (strength > bestStrength) {
                    bestStrength = strength;
                }
            }

            int score;
            if (bestStrength <= RED_STRENGTH) {
                score = 0;
            } else if (bestStrength < GREEN_STRENGTH) {
                score = 1;
            } else {
                score = 2;
            }
            worstScore = Math.min(worstScore, score);
        }

        if (worstScore == 0) return COLOR_RED;
        if (worstScore == 1) return COLOR_YELLOW;
        return COLOR_GREEN;
    }

    @Subscribe
    public void oe$BottomSkia(EventHUD.BottomSkia event) {
        Canvas canvas = Client.INSTANCE.skia.getCanvas();

        ItemStack[] items = mc.thePlayer.inventory.mainInventory;

        for (BlockRef ref : blocks) {
            BlockPos blockPos = ref.pos;
            Vec3 pos = screenPositions.get(blockPos);
            if (pos == null || pos.zCoord < 0) continue;

            List<ItemStack> blocks = defenseBlocks.get(blockPos);
            if (blocks == null || blocks.isEmpty()) continue;

            int stateColor = getDefenseStateColor(blocks, items);

            float baseRectW = 40;
            float rectH = 40;
            float itemSize = 32f;
            float padding = 8f;

            float rectW = Math.max(baseRectW, (blocks.size() + 1) * itemSize + padding);

            float x = (float) pos.xCoord - rectW / 2;
            float y = (float) pos.yCoord - rectH / 2;

            canvas.drawRectShadowNoclip(Rect.makeXYWH(x, y, rectW, rectH), 0f, 0f, 10f, 0f, 0x88000000);

            Font font = Fonts.MaterialIcons
                    .fill(true)
                    .opticSize(24)
                    .weight(900);

            try (Paint p = new Paint()) {
                p.setColor(0xFF303030);
                Renderer.drawRRect(x, y, rectW, rectH, 8f, p);
                p.setStroke(true);
                p.setStrokeWidth(2f);
                p.setColor(0xFF242424);
                Renderer.drawRRect(x, y, rectW, rectH, 8f, p);

                String icon = "";
                switch (stateColor) {
                    case COLOR_RED:
                        icon = MaterialIcons.CLOSE;
                        break;
                    case COLOR_YELLOW:
                        icon = MaterialIcons.WARNING;
                        break;
                    case COLOR_GREEN:
                        icon = MaterialIcons.CHECK;
                        break;
                }

                p.setStroke(false);
                p.setColor(stateColor);
                Renderer.drawText(icon, x + rectW - rectH / 2f, y + rectH / 2f, font, 18f, Font.Align.CENTER, p);
            }
        }
    }

    @Subscribe
    public void oe$TopVanilla(EventHUD.TopVanilla event) {
        Renderer2D.begin2d();
        for (BlockRef ref : blocks) {
            BlockPos blockPos = ref.pos;
            Vec3 pos = screenPositions.get(blockPos);
            if (pos == null || pos.zCoord < 0) continue;

            List<ItemStack> blocks = defenseBlocks.get(blockPos);
            if (blocks == null || blocks.isEmpty()) continue;

            float x = (float) pos.xCoord;
            float y = (float) pos.yCoord;

            float baseRectW = 40;
            float rectH = 40;

            float rectW = baseRectW;

            int count = blocks.size() + 1;
            float startX = x / 2 - (count * 16f) / 2f;

            int i = 0;
            for (ItemStack item : blocks) {
                GlStateManager.pushMatrix();
                GlStateManager.scale(2, 2, 1);
                Renderer2D.drawItem(item, startX + i * 16f, (y - 16) / 2);
                GlStateManager.popMatrix();
                i++;
            }
        }
        Renderer2D.end2d();
    }
}
