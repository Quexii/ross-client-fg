package eu.shoroa.ross.module.impl.render;

import eu.shoroa.ross.event.EventRender3D;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.mixins.injection.client.MinecraftAccessor;
import eu.shoroa.ross.mixins.injection.client.renderer.entity.RenderManagerAccessor;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.settings.BooleanSetting;
import eu.shoroa.ross.settings.ColorSetting;
import eu.shoroa.ross.settings.NumberSetting;
import eu.shoroa.ross.util.render.Renderer3D;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static eu.shoroa.ross.Client.mc;

public class ModuleTrajectories extends Module {
    private final ColorSetting lineColor = register(new ColorSetting("Line Color", "trajectories.line_color", new Color(200, 50, 50, 255)));
    private final ColorSetting hitColor = register(new ColorSetting("Hit Color", "trajectories.hit_color", new Color(200, 50, 50, 255)));
    private final NumberSetting lineWidth = register(new NumberSetting("Line Width", "trajectories.line_width", 2.0f, 0.5f, 5.0f, 0.5f));
    private final BooleanSetting showHit = register(new BooleanSetting("Landing Indicator", "trajectories.landing_indicator", true));
    private final BooleanSetting entityHit = register(new BooleanSetting("Entity Indicator", "trajectories.entity_indicator", true));
    private final NumberSetting hitRadius = register(new NumberSetting("Indicator Radius", "trajectories.indicator_radius", 0.25f, 0.05f, 1.0f, 0.05f));

    public ModuleTrajectories() {
        super("Trajectories", "Draws the path based on projectile type and velocity", Category.RENDER, null);
    }

    @Subscribe
    public void oe$OnRender3D(EventRender3D event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        ItemStack heldItem = mc.thePlayer.getCurrentEquippedItem();
        if (heldItem == null) return;

        Item item = heldItem.getItem();
        ItemVelocity velocity = getVelocity(item, heldItem);
        if (velocity == null || velocity.speed == 0.0f) return;

        float partialTicks = ((MinecraftAccessor) mc).getTimer().renderPartialTicks;
        double posX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
        double posY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks + mc.thePlayer.getEyeHeight();
        double posZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;

        float yaw = mc.thePlayer.prevRotationYaw + (mc.thePlayer.rotationYaw - mc.thePlayer.prevRotationYaw) * partialTicks;
        float pitch = mc.thePlayer.prevRotationPitch + (mc.thePlayer.rotationPitch - mc.thePlayer.prevRotationPitch) * partialTicks;

        double motionX = -MathHelper.sin(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI);
        double motionY = -MathHelper.sin((pitch + velocity.pitchOffset) / 180.0F * (float) Math.PI);
        double motionZ = MathHelper.cos(yaw / 180.0F * (float) Math.PI) * MathHelper.cos(pitch / 180.0F * (float) Math.PI);

        double magnitude = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX = (motionX / magnitude) * velocity.speed;
        motionY = (motionY / magnitude) * velocity.speed;
        motionZ = (motionZ / magnitude) * velocity.speed;

        posX -= MathHelper.cos(yaw / 180.0F * (float) Math.PI) * 0.16F;
        posY -= 0.1D;
        posZ -= MathHelper.sin(yaw / 180.0F * (float) Math.PI) * 0.16F;

        List<Vec3> points = new ArrayList<>();
        MovingObjectPosition hitResult = null;
        boolean landed = false;

        points.add(new Vec3(posX, posY, posZ));

        for (int i = 0; i < 300; i++) {
            Vec3 currentPos = new Vec3(posX, posY, posZ);
            Vec3 nextPos = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);

            MovingObjectPosition blockHit = mc.theWorld.rayTraceBlocks(currentPos, nextPos, false, true, false);
            if (blockHit != null && blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                hitResult = blockHit;
                points.add(new Vec3(blockHit.hitVec.xCoord, blockHit.hitVec.yCoord, blockHit.hitVec.zCoord));
                landed = true;
                break;
            }

            AxisAlignedBB motionBB = new AxisAlignedBB(posX, posY, posZ, posX, posY, posZ)
                    .expand(0.3, 0.3, 0.3)
                    .addCoord(motionX, motionY, motionZ);

            List<Entity> entities = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.thePlayer, motionBB);
            double closestDist = Double.MAX_VALUE;
            Entity closestEntity = null;
            Vec3 closestHitVec = null;

            for (Entity entity : entities) {
                if (!entity.canBeCollidedWith()) continue;
                float border = entity.getCollisionBorderSize();
                AxisAlignedBB entityBB = entity.getEntityBoundingBox().expand(border, border, border);
                MovingObjectPosition entityHit = entityBB.calculateIntercept(currentPos, nextPos);
                if (entityHit != null) {
                    double dist = currentPos.squareDistanceTo(entityHit.hitVec);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestEntity = entity;
                        closestHitVec = entityHit.hitVec;
                    }
                }
            }

            if (closestEntity != null) {
                hitResult = new MovingObjectPosition(closestEntity, closestHitVec);
                points.add(closestHitVec);
                landed = true;
                break;
            }

            posX += motionX;
            posY += motionY;
            posZ += motionZ;
            points.add(new Vec3(posX, posY, posZ));

            float drag = velocity.isInWater(posX, posY, posZ, mc) ? 0.8F : 0.99F;
            motionX *= drag;
            motionY *= drag;
            motionZ *= drag;

            motionY -= velocity.gravity;

            if (posY < -64) break;
        }

        if (points.size() < 2) return;

        double renderX = ((RenderManagerAccessor) mc.getRenderManager()).getRenderPosX();
        double renderY = ((RenderManagerAccessor) mc.getRenderManager()).getRenderPosY();
        double renderZ = ((RenderManagerAccessor) mc.getRenderManager()).getRenderPosZ();

        Color lc = lineColor.get();
        int lr = lc.getRed(), lg = lc.getGreen(),
                lb = lc.getBlue(), la = lc.getAlpha();

        Renderer3D.begin3D(lineWidth.get());

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();

        wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (Vec3 p : points) {
            wr.pos(p.xCoord - renderX, p.yCoord - renderY, p.zCoord - renderZ).color(lr, lg, lb, la).endVertex();
        }
        tessellator.draw();

        if (landed && showHit.get()) {
            Color hc = hitColor.get();
            int hr = hc.getRed(), hg = hc.getGreen(), hb = hc.getBlue(), ha = hc.getAlpha();

            double hx = hitResult.hitVec.xCoord - renderX;
            double hy = hitResult.hitVec.yCoord - renderY;
            double hz = hitResult.hitVec.zCoord - renderZ;
            float r = hitRadius.get();

            if (hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                Renderer3D.drawFilledHitCircle(hx, hy, hz, r, hitResult.sideHit, hr, hg, hb, ha / 3, 32);
                GL11.glLineWidth(2.0f);
                Renderer3D.drawHitCircle(hx, hy, hz, r, hitResult.sideHit, hr, hg, hb, ha, 32);
                GL11.glLineWidth(1.5f);
                Renderer3D.drawCross(hx, hy, hz, r * 0.6f, hitResult.sideHit, hr, hg, hb, ha);
            } else {
                GL11.glLineWidth(2.0f);
                Renderer3D.drawHitCircle(hx, hy, hz, r, EnumFacing.UP, hr, hg, hb, ha, 32);
                Renderer3D.drawFilledHitCircle(hx, hy, hz, r, EnumFacing.UP, hr, hg, hb, ha / 4, 32);
            }
        }

        if (landed && entityHit.get() && hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            Color hc = hitColor.get();

            Renderer3D.drawBoxWireframe(hitResult.entityHit.getEntityBoundingBox().offset(-renderX, -renderY, -renderZ), hc.getRGB());
            Renderer3D.drawBoxFilled(hitResult.entityHit.getEntityBoundingBox().offset(-renderX, -renderY, -renderZ), hc.getRGB() & 0x7FFFFFFF);
        }

        Renderer3D.end3D();
    }

    private ItemVelocity getVelocity(Item item, ItemStack stack) {
        if (item instanceof ItemBow) {
            int useCount = mc.thePlayer.getItemInUseCount();
            if (useCount <= 0 && !mc.thePlayer.isUsingItem()) return null;
            float charge = (float) (72000 - useCount) / 20.0F;
            charge = (charge * charge + charge * 2.0F) / 3.0F;
            if (charge < 0.1F) return null;
            if (charge > 1.0F) charge = 1.0F;
            return new ItemVelocity(charge * 3.0F, 0.0F, 0.05F);
        } else if (item instanceof ItemEgg || item instanceof ItemEnderPearl || item instanceof ItemSnowball) {
            return new ItemVelocity(1.5F, 0.0F, 0.03F);
        } else if (item instanceof ItemPotion && ItemPotion.isSplash(stack.getMetadata())) {
            return new ItemVelocity(0.5F, -20.0F, 0.05F);
        } else if (item instanceof ItemExpBottle) {
            return new ItemVelocity(0.7F, -20.0F, 0.07F);
        }
        return null;
    }

    private static class ItemVelocity {
        final float speed;
        final float pitchOffset;
        final float gravity;

        ItemVelocity(float speed, float pitchOffset, float gravity) {
            this.speed = speed;
            this.pitchOffset = pitchOffset;
            this.gravity = gravity;
        }

        boolean isInWater(double x, double y, double z, Minecraft mc) {
            BlockPos pos = new BlockPos(
                    MathHelper.floor_double(x),
                    MathHelper.floor_double(y),
                    MathHelper.floor_double(z));
            IBlockState state = mc.theWorld.getBlockState(pos);
            return state.getBlock().getMaterial() == Material.water;
        }
    }
}
