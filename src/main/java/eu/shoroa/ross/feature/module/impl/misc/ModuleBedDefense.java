package eu.shoroa.ross.feature.module.impl.misc;

import eu.shoroa.ross.event.EventRenderEntities;
import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.module.impl.hud.StellaHud;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.opengl.Renderer2D;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.utils.proj.Projection;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Path;
import io.github.humbleui.types.Point;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static eu.shoroa.ross.Client.mc;

public class ModuleBedDefense extends Module {
    private static final float CELL = 26f;
    private static final float ITEM = 22f;
    private static final float GAP = 4f;
    private static final float PAD = 5f;
    private static final float STATUS_W = 20f;
    private static final float RADIUS = 8f;
    private static final float CHEVRON = 12f;

    private static final int COLOR_HARD = 0xFFEF5350;
    private static final int COLOR_SLOW = 0xFFF0A030;
    private static final int COLOR_EASY = 0xFF43A86F;

    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final int MAX_BEDS = 16;

    private final SettingCategory settings = addCategory("Settings", ".", "settings");
    private final NumberSetting maxDistance = register(new NumberSetting("Max distance", "max_distance", 48, 16, 96, 1), settings);
    private final NumberSetting maxLayers = register(new NumberSetting("Max layers", "max_layers", 4, 1, 5, 1), settings);
    private final BooleanSetting showBreakability = register(new BooleanSetting("Show breakability", "breakability", true), settings);

    private static class BedInfo {
        final BlockPos head;
        final double x, y, z;
        /** Dominant block of each defense shell, innermost first. */
        final List<ItemStack> layers;
        final boolean exposed;
        Vec3 screen;

        BedInfo(BlockPos head, double x, double y, double z, List<ItemStack> layers, boolean exposed) {
            this.head = head;
            this.x = x;
            this.y = y;
            this.z = z;
            this.layers = layers;
            this.exposed = exposed;
        }
    }

    private List<BedInfo> beds = new ArrayList<>();
    private int tickCounter;

    public ModuleBedDefense() {
        super("Bed Defense", "Shows which blocks nearby bed defenses use", Category.MISC, MaterialIcons.BED);
    }

    @Override
    public void onDisable() {
        beds = new ArrayList<>();
        super.onDisable();
    }

    ///////////////////////////////
    // DETECTION
    ///////////////////////////////

    @Subscribe
    @ApiStatus.Internal
    public void onTick(EventTick event) {
        if (mc.theWorld == null || mc.thePlayer == null) {
            beds = new ArrayList<>();
            return;
        }
        if (++tickCounter % SCAN_INTERVAL_TICKS != 0) return;

        int radius = (int) (float) maxDistance.get();
        int chunkRadius = (radius >> 4) + 1;
        int playerChunkX = MathHelper.floor_double(mc.thePlayer.posX) >> 4;
        int playerChunkZ = MathHelper.floor_double(mc.thePlayer.posZ) >> 4;

        List<BedInfo> found = new ArrayList<>();
        for (int cx = -chunkRadius; cx <= chunkRadius && found.size() < MAX_BEDS; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius && found.size() < MAX_BEDS; cz++) {
                Chunk chunk = mc.theWorld.getChunkFromChunkCoords(playerChunkX + cx, playerChunkZ + cz);
                if (chunk == null || !chunk.isLoaded()) continue;

                for (ExtendedBlockStorage storage : chunk.getBlockStorageArray()) {
                    if (storage == null) continue;

                    for (int y = 0; y < 16 && found.size() < MAX_BEDS; y++) {
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                IBlockState state = storage.get(x, y, z);
                                if (!(state.getBlock() instanceof BlockBed)) continue;
                                if (state.getValue(BlockBed.PART) != BlockBed.EnumPartType.HEAD) continue;

                                BlockPos pos = new BlockPos(
                                        (chunk.xPosition << 4) + x,
                                        storage.getYLocation() + y,
                                        (chunk.zPosition << 4) + z);
                                if (mc.thePlayer.getDistanceSq(pos) > (double) radius * radius) continue;

                                found.add(analyze(pos));
                            }
                        }
                    }
                }
            }
        }
        beds = found;
    }

    /**
     * Walks outward from the bed in shells: shell N is every block touching
     * shell N-1 (or the bed itself for N=1). A shell that contains air or a
     * non-solid block means the bed is reachable there, so the defense ends.
     * Blocks below bed level are treated as ground and ignored entirely.
     */
    private BedInfo analyze(BlockPos head) {
        Set<BlockPos> region = new HashSet<>();
        region.add(head);

        BlockPos foot = null;
        for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            BlockPos neighbor = head.offset(facing);
            if (mc.theWorld.getBlockState(neighbor).getBlock() instanceof BlockBed) {
                foot = neighbor;
                region.add(neighbor);
                break;
            }
        }

        int bedY = head.getY();
        List<ItemStack> layers = new ArrayList<>();
        boolean exposed = false;

        for (int depth = 1; depth <= (int) (float) maxLayers.get(); depth++) {
            Set<BlockPos> shell = new HashSet<>();
            for (BlockPos pos : region) {
                for (EnumFacing facing : EnumFacing.values()) {
                    BlockPos neighbor = pos.offset(facing);
                    if (neighbor.getY() < bedY || region.contains(neighbor)) continue;
                    shell.add(neighbor);
                }
            }

            boolean open = false;
            for (BlockPos pos : shell) {
                Block block = mc.theWorld.getBlockState(pos).getBlock();
                if (mc.theWorld.isAirBlock(pos) || !block.getMaterial().isSolid()) {
                    open = true;
                    break;
                }
            }
            if (open) {
                exposed = depth == 1;
                break;
            }

            // The shell is sealed: record its dominant block as this layer.
            Map<Integer, Integer> counts = new HashMap<>();
            Map<Integer, ItemStack> samples = new HashMap<>();
            for (BlockPos pos : shell) {
                IBlockState state = mc.theWorld.getBlockState(pos);
                Block block = state.getBlock();
                if (block instanceof BlockBed || Item.getItemFromBlock(block) == null) continue;

                int meta = block.getMetaFromState(state);
                int key = (Block.getIdFromBlock(block) << 4) | (meta & 0xF);
                counts.merge(key, 1, Integer::sum);
                samples.putIfAbsent(key, new ItemStack(block, 1, meta));
            }

            ItemStack dominant = null;
            int maxCount = 0;
            for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    dominant = samples.get(entry.getKey());
                }
            }
            if (dominant == null) break;

            layers.add(dominant);
            region.addAll(shell);
        }

        double centerX = foot != null ? (head.getX() + foot.getX()) / 2.0 + 0.5 : head.getX() + 0.5;
        double centerZ = foot != null ? (head.getZ() + foot.getZ()) / 2.0 + 0.5 : head.getZ() + 0.5;
        return new BedInfo(head, centerX, bedY + 0.5, centerZ, layers, exposed);
    }

    /** Worst layer vs the best tool in the player's inventory. */
    private int breakabilityColor(BedInfo bed) {
        if (bed.layers.isEmpty()) return COLOR_EASY;

        ItemStack[] inventory = mc.thePlayer.inventory.mainInventory;
        int worst = 2;
        for (ItemStack layer : bed.layers) {
            Block block = Block.getBlockFromItem(layer.getItem());
            if (block == null) continue;

            float best = 1f;
            for (ItemStack stack : inventory) {
                if (stack == null) continue;
                best = Math.max(best, stack.getStrVsBlock(block));
            }

            int score = best <= 1f ? 0 : best < 4f ? 1 : 2;
            worst = Math.min(worst, score);
        }

        return worst == 0 ? COLOR_HARD : worst == 1 ? COLOR_SLOW : COLOR_EASY;
    }

    ///////////////////////////////
    // PROJECTION
    ///////////////////////////////

    @Subscribe
    @ApiStatus.Internal
    public void onRenderEntitiesPost(EventRenderEntities.Post event) {
        double renderX = mc.getRenderManager().viewerPosX;
        double renderY = mc.getRenderManager().viewerPosY;
        double renderZ = mc.getRenderManager().viewerPosZ;

        for (BedInfo bed : beds) {
            bed.screen = Projection.w2s(
                    (float) (bed.x - renderX),
                    (float) (bed.y - renderY),
                    (float) (bed.z - renderZ));
        }
    }

    ///////////////////////////////
    // RENDERING
    ///////////////////////////////

    @Subscribe
    @ApiStatus.Internal
    public void onHud(Hud.Layer event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (event.is(Hud.Layer.NAME_SKIA_BOTTOM)) {
            try (Paint p = new Paint()) {
                for (BedInfo bed : beds) {
                    if (onScreen(bed)) drawCard(bed, p);
                }
            }
        } else if (event.is(Hud.Layer.NAME_VANILLA_BOTTOM)) {
            for (BedInfo bed : beds) {
                if (onScreen(bed)) drawItems(bed);
            }
        }
    }

    private boolean onScreen(BedInfo bed) {
        return bed.screen != null
                && bed.screen.xCoord >= -100 && bed.screen.xCoord <= mc.displayWidth + 100
                && bed.screen.yCoord >= -100 && bed.screen.yCoord <= mc.displayHeight + 100;
    }

    private int cellCount(BedInfo bed) {
        // An exposed/undefended bed still gets one cell for the bed icon.
        return Math.max(1, bed.layers.size());
    }

    private float cardWidth(BedInfo bed) {
        int cells = cellCount(bed);
        float width = PAD * 2f + cells * CELL + (cells - 1) * GAP;
        if (showBreakability.get()) width += GAP + STATUS_W;
        return width;
    }

    private float cardX(BedInfo bed) {
        return (float) bed.screen.xCoord - cardWidth(bed) / 2f;
    }

    private float cardY(BedInfo bed) {
        return (float) bed.screen.yCoord - CHEVRON - (PAD * 2f + CELL);
    }

    private void drawCard(BedInfo bed, Paint p) {
        StellaTheme t = StellaTheme.get();
        float w = cardWidth(bed);
        float h = PAD * 2f + CELL;
        float x = cardX(bed);
        float y = cardY(bed);
        float px = (float) bed.screen.xCoord;

        // Tail chevron pointing down at the bed center.
        try (Path tail = Path.makePolygon(new Point[]{
                new Point(px, y + h + CHEVRON - 2f),
                new Point(px - 6f, y + h - 2f),
                new Point(px + 6f, y + h - 2f)
        }, true)) {
            p.setColor(t.accent);
            UI.drawPath(tail, p);
        }

        StellaHud.card(x, y, w, h, RADIUS, p);

        // Item cells.
        for (int i = 0; i < cellCount(bed); i++) {
            float cellX = x + PAD + i * (CELL + GAP);
            p.setColor(t.surfaceDim);
            UI.drawRRect(cellX, y + PAD, CELL, CELL, RADIUS * 0.6f, p);
            p.setStroke(true);
            p.setStrokeWidth(1.5f);
            p.setColor(t.border);
            UI.drawRRect(cellX, y + PAD, CELL, CELL, RADIUS * 0.6f, p);
            p.setStroke(false);
        }

        // No defense: bed icon instead of a block.
        if (bed.layers.isEmpty()) {
            p.setColor(bed.exposed ? COLOR_EASY : t.foregroundMuted);
            UI.drawText(MaterialIcons.BED, x + PAD + CELL / 2f, y + PAD + CELL / 2f,
                    Fonts.MaterialIcons.weight(400).fill(true), 18f, Align.CENTER, p);
        }

        if (showBreakability.get()) {
            int color = breakabilityColor(bed);
            String icon = color == COLOR_HARD ? MaterialIcons.CLOSE
                    : color == COLOR_SLOW ? MaterialIcons.WARNING
                    : MaterialIcons.CHECK;

            p.setColor(color);
            UI.drawText(icon, x + w - PAD - STATUS_W / 2f, y + h / 2f,
                    Fonts.MaterialIcons.weight(700).fill(true), 17f, Align.CENTER, p);
        }

        drawDistance(px, y - 10f, (float) mc.thePlayer.getDistance(bed.x, bed.y, bed.z), p);
    }

    private void drawDistance(float x, float y, float distance, Paint p) {
        StellaTheme t = StellaTheme.get();
        String label = (int) distance + "m";

        p.setStroke(true);
        p.setStrokeWidth(3f);
        p.setColor(t.surface);
        UI.drawText(label, x, y, Fonts.GoogleFlex.weight(600).opticSize(14), 14f, Align.CENTER, p);

        p.setStroke(false);
        p.setColor(t.foreground);
        UI.drawText(label, x, y, Fonts.GoogleFlex.weight(600).opticSize(14), 14f, Align.CENTER, p);
    }

    private void drawItems(BedInfo bed) {
        if (bed.layers.isEmpty()) return;

        float x = cardX(bed);
        float y = cardY(bed);
        float inset = (CELL - ITEM) / 2f;
        int scale = new ScaledResolution(mc).getScaleFactor();

        GlStateManager.pushMatrix();
        GlStateManager.scale(1f / scale, 1f / scale, 1f);
        for (int i = 0; i < bed.layers.size(); i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + PAD + i * (CELL + GAP) + inset, y + PAD + inset, 0f);
            GlStateManager.scale(ITEM / 16f, ITEM / 16f, 1f);
            Renderer2D.drawItem(bed.layers.get(i), 0f, 0f);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }
}
