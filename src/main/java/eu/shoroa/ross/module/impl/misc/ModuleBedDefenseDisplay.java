package eu.shoroa.ross.module.impl.misc;

import eu.shoroa.ross.event.*;
import eu.shoroa.ross.mixins.injection.client.renderer.entity.RenderManagerAccessor;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.types.BlockRef;
import eu.shoroa.ross.util.proj.Projection;
import eu.shoroa.ross.util.render.Renderer2D;
import eu.shoroa.ross.util.render.Renderer3D;
import eu.shoroa.ross.util.world.WorldHelper;
import io.github.humbleui.skija.Paint;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.Chunk;

import java.util.*;

import static eu.shoroa.ross.Client.mc;

public class ModuleBedDefenseDisplay extends Module {
    private List<BlockRef> blocks = new ArrayList<>();
    private Map<BlockPos, List<Block>> defenseBlocks = new HashMap<>();
    private Map<BlockPos, Vec3> screenPositions = new HashMap<>();

    private static final Set<Block> targets = new HashSet<>(Arrays.asList(Blocks.bed));
    private static final int DEFENSE_RADIUS = 1;
    private static final int DEFENSE_MAX_Y_OFFSET = 3;
    private static final int[][] CARDINAL_DIRS = new int[][]{
            {1, 0}, {-1, 0}, {0, 1}, {0, -1}
    };

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

            List<Block> defenseLayers = collectDefenseLayers(pos);
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

    private List<Block> collectDefenseLayers(BlockPos bedPos) {
        Set<BlockPos> bedParts = new HashSet<>();
        bedParts.add(bedPos);
        for (int[] dir : CARDINAL_DIRS) {
            BlockPos neighbor = bedPos.add(dir[0], 0, dir[1]);
            if (mc.theWorld.getBlockState(neighbor).getBlock() instanceof BlockBed) {
                bedParts.add(neighbor);
            }
        }

        List<Block> layers = new ArrayList<>();
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

            Map<Block, Integer> counts = new HashMap<>();
            for (BlockPos checkPos : ringPositions) {
                if (mc.theWorld.isAirBlock(checkPos)) {
                    continue;
                }
                Block block = mc.theWorld.getBlockState(checkPos).getBlock();
                if (block instanceof BlockBed) continue;
                counts.put(block, counts.getOrDefault(block, 0) + 1);
            }

            Block dominant = null;
            int maxCount = 0;
            for (Map.Entry<Block, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    dominant = entry.getKey();
                    maxCount = entry.getValue();
                }
            }
            if (dominant != null) {
                layers.add(dominant);
            }
        }

        return layers;
    }

    @Subscribe
    public void oe$BottomSkia(EventHUD.BottomSkia event) {
        for (BlockRef ref : blocks) {
            BlockPos blockPos = ref.pos;
            Vec3 pos = screenPositions.get(blockPos);
            if (pos == null || pos.zCoord < 0) continue;

            List<Block> blocks = defenseBlocks.get(blockPos);
            if (blocks == null || blocks.isEmpty()) continue;

            float baseRectW = 40;
            float rectH = 40;
            float itemSize = 32f;
            float padding = 8f;

            float rectW = Math.max(baseRectW, blocks.size() * itemSize + padding);

            try (Paint p = new Paint()) {
                p.setColor(0xFF242424);
                Renderer.drawRRect((float) (pos.xCoord - rectW / 2), (float) (pos.yCoord - rectH / 2), rectW, rectH, 8f, p);
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

            List<Block> blocks = defenseBlocks.get(blockPos);
            if (blocks == null || blocks.isEmpty()) continue;

            float x = (float) pos.xCoord;
            float y = (float) pos.yCoord;

            float baseRectW = 40;
            float rectH = 40;

            float rectW = baseRectW;

            int count = blocks.size();
            float startX = x / 2 - (count * 16f) / 2f;

            int i = 0;
            for (Block block : blocks) {
                ItemStack item = new ItemStack(block);
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
