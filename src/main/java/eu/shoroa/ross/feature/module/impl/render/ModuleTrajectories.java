package eu.shoroa.ross.feature.module.impl.render;

import eu.shoroa.ross.event.EventRender3D;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.opengl.Renderer3D;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.List;

import static eu.shoroa.ross.Client.mc;

public class ModuleTrajectories extends Module {
    private static final int MAX_TICKS = 200;

    private final SettingCategory settings = addCategory("Settings", "settings", "settings");
    private final BooleanSetting idleBow = register(new BooleanSetting("Idle bow preview", "idle_bow", true), settings);
    private final BooleanSetting hitMarker = register(new BooleanSetting("Landing marker", "marker", true), settings);

    public ModuleTrajectories() {
        super("Trajectories", "Predicts the flight path of projectiles", Category.RENDER, MaterialIcons.ROUTE);
    }

    @Subscribe
    @ApiStatus.Internal
    public void onRender3D(EventRender3D event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Launch spec = specFor(mc.thePlayer);
        if (spec == null) return;

        Simulation sim = simulate(mc.thePlayer, spec, event.partialTicks);
        if (sim.points.size() < 2) return;

        StellaTheme t = StellaTheme.get();
        double rx = mc.getRenderManager().viewerPosX;
        double ry = mc.getRenderManager().viewerPosY;
        double rz = mc.getRenderManager().viewerPosZ;

        Renderer3D.begin3D(2.5f);
        for (int i = 1; i < sim.points.size(); i++) {
            Vec3 a = sim.points.get(i - 1);
            Vec3 b = sim.points.get(i);
            Renderer3D.drawLine(
                    a.xCoord - rx, a.yCoord - ry, a.zCoord - rz,
                    b.xCoord - rx, b.yCoord - ry, b.zCoord - rz,
                    t.accent);
        }

        if (hitMarker.get() && sim.hit != null) {
            if (sim.hit.entityHit != null) {
                AxisAlignedBB box = sim.hit.entityHit.getEntityBoundingBox().expand(0.1, 0.1, 0.1)
                        .offset(-rx, -ry, -rz);
                Renderer3D.drawBoxFilled(box, withAlpha(t.accent, 0.15f));
                Renderer3D.drawBoxWireframe(box, t.accent);
            } else if (sim.hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                drawLandingMarker(sim.hit, rx, ry, rz, t.accent);
            }
        }
        Renderer3D.end3D();
    }

    private void drawLandingMarker(MovingObjectPosition hit, double rx, double ry, double rz, int accent) {
        double x = hit.hitVec.xCoord - rx;
        double y = hit.hitVec.yCoord - ry;
        double z = hit.hitVec.zCoord - rz;
        EnumFacing face = hit.sideHit;

        int r = accent >> 16 & 0xFF;
        int g = accent >> 8 & 0xFF;
        int b = accent & 0xFF;

        Renderer3D.drawFilledHitCircle(x, y, z, 0.35f, face, r, g, b, 45, 24);
        Renderer3D.drawHitCircle(x, y, z, 0.35f, face, r, g, b, 220, 24);

        float pulse = (System.currentTimeMillis() % 1200L) / 1200f;
        Renderer3D.drawHitCircle(x, y, z, 0.35f + pulse * 0.3f, face, r, g, b, (int) ((1f - pulse) * 160f), 24);
    }

    private Launch specFor(EntityPlayer player) {
        ItemStack stack = player.getHeldItem();
        if (stack == null) return null;
        Item item = stack.getItem();

        if (item instanceof ItemBow) {
            float charge = 1f;
            if (player.isUsingItem()) {
                float f = player.getItemInUseDuration() / 20f;
                f = (f * f + f * 2f) / 3f;
                if (f < 0.1f) return null;
                charge = Math.min(f, 1f);
            } else if (!idleBow.get()) {
                return null;
            }
            return new Launch(charge * 2f * 1.5f, 0.05, 0f);
        }
        if (item == Items.snowball || item == Items.egg || item == Items.ender_pearl) {
            return new Launch(1.5, 0.03, 0f);
        }
        if (item == Items.experience_bottle) {
            return new Launch(0.7, 0.07, -20f);
        }
        if (item instanceof ItemPotion && ItemPotion.isSplash(stack.getMetadata())) {
            return new Launch(0.5, 0.05, -20f);
        }
        return null;
    }

    private Simulation simulate(EntityPlayer player, Launch spec, float partialTicks) {
        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        float yawRad = (float) Math.toRadians(yaw);
        float dirPitchRad = (float) Math.toRadians(pitch + spec.pitchOffset);

        double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;

        double x = px - MathHelper.cos(yawRad) * 0.16;
        double y = py + player.getEyeHeight() - 0.1;
        double z = pz - MathHelper.sin(yawRad) * 0.16;

        double dx = -MathHelper.sin(yawRad) * MathHelper.cos(dirPitchRad);
        double dy = -MathHelper.sin(dirPitchRad);
        double dz = MathHelper.cos(yawRad) * MathHelper.cos(dirPitchRad);
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double mx = dx / len * spec.velocity;
        double my = dy / len * spec.velocity;
        double mz = dz / len * spec.velocity;

        Simulation sim = new Simulation();
        sim.points.add(new Vec3(x, y, z));

        for (int tick = 0; tick < MAX_TICKS; tick++) {
            double nx = x + mx, ny = y + my, nz = z + mz;

            MovingObjectPosition hit = mc.theWorld.rayTraceBlocks(new Vec3(x, y, z), new Vec3(nx, ny, nz), false, true, false);
            MovingObjectPosition entityHit = interceptEntity(player, new Vec3(x, y, z),
                    hit != null ? hit.hitVec : new Vec3(nx, ny, nz));

            if (entityHit != null) hit = entityHit;
            if (hit != null) {
                sim.points.add(hit.hitVec);
                sim.hit = hit;
                break;
            }

            x = nx;
            y = ny;
            z = nz;
            sim.points.add(new Vec3(x, y, z));

            mx *= 0.99;
            my *= 0.99;
            mz *= 0.99;
            my -= spec.gravity;
        }

        return sim;
    }

    private MovingObjectPosition interceptEntity(EntityPlayer player, Vec3 start, Vec3 end) {
        AxisAlignedBB sweep = new AxisAlignedBB(
                Math.min(start.xCoord, end.xCoord), Math.min(start.yCoord, end.yCoord), Math.min(start.zCoord, end.zCoord),
                Math.max(start.xCoord, end.xCoord), Math.max(start.yCoord, end.yCoord), Math.max(start.zCoord, end.zCoord)
        ).expand(0.3, 0.3, 0.3);

        MovingObjectPosition closest = null;
        double closestDist = Double.MAX_VALUE;

        for (Entity entity : mc.theWorld.getEntitiesWithinAABBExcludingEntity(player, sweep)) {
            if (!entity.canBeCollidedWith()) continue;

            MovingObjectPosition intercept = entity.getEntityBoundingBox().expand(0.3, 0.3, 0.3).calculateIntercept(start, end);
            if (intercept == null) continue;

            double dist = start.squareDistanceTo(intercept.hitVec);
            if (dist < closestDist) {
                closestDist = dist;
                closest = intercept;
                closest.entityHit = entity;
            }
        }

        return closest;
    }

    private static int withAlpha(int color, float alpha) {
        int a = (int) (((color >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, alpha)));
        return (a << 24) | (color & 0xFFFFFF);
    }

    private static final class Launch {
        final double velocity;
        final double gravity;
        final float pitchOffset;

        Launch(double velocity, double gravity, float pitchOffset) {
            this.velocity = velocity;
            this.gravity = gravity;
            this.pitchOffset = pitchOffset;
        }
    }

    private static final class Simulation {
        final List<Vec3> points = new ArrayList<>();
        MovingObjectPosition hit;
    }
}
