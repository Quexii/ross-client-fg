package eu.shoroa.ross.util.proj;

import eu.shoroa.ross.event.EventPostEntityRender;
import eu.shoroa.ross.mixins.injection.client.MinecraftAccessor;
import eu.shoroa.ross.types.Point;
import eu.shoroa.ross.types.Rect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.vecmath.Vector2f;
import java.util.HashMap;
import java.util.Map;

public class EntityProjection {
    private static EntityProjection instance;

    private static final Minecraft mc = Minecraft.getMinecraft();

    private final Map<Entity, Rect> positionMap = new HashMap<>();

    public EntityProjection() {
        instance = this;

        MinecraftForge.EVENT_BUS.register(this);
    }

    public static EntityProjection getInstance() {
        if (instance == null) {
            instance = new EntityProjection();
        }
        return instance;
    }

    private Rect projEntity(Entity entity) {
        RenderManager renderManager = mc.getRenderManager();
        double viewerX = renderManager.viewerPosX;
        double viewerY = renderManager.viewerPosY;
        double viewerZ = renderManager.viewerPosZ;
        float partialTicks = ((MinecraftAccessor) mc).getTimer().renderPartialTicks;

        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - viewerX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - viewerY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - viewerZ;

        double halfWidth = entity.width / 2.0;
        double height = entity.height;

        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        boolean visible = false;

        for (int i = 0; i < 8; i++) {
            double cornerX = (i & 1) == 0 ? x - halfWidth : x + halfWidth;
            double cornerY = (i & 2) == 0 ? y : y + height;
            double cornerZ = (i & 4) == 0 ? z - halfWidth : z + halfWidth;

            Vec3 screenPos = Projection.w2s((float) cornerX, (float) cornerY, (float) cornerZ);

            if (screenPos != null && screenPos.zCoord > 0) {
                visible = true;
                minX = (float) Math.min(minX, screenPos.xCoord);
                minY = (float) Math.min(minY, screenPos.yCoord);
                maxX = (float) Math.max(maxX, screenPos.xCoord);
                maxY = (float) Math.max(maxY, screenPos.yCoord);
            }
        }

        if (visible) {
            minX = Math.max(0, minX);
            minY = Math.max(0, minY);
            maxX = Math.min(mc.displayWidth, maxX);
            maxY = Math.min(mc.displayHeight, maxY);

            return new Rect(minX, minY, maxX - minX, maxY - minY);
        }

        return null;
    }

    @SubscribeEvent
    public void oe$PostEntityRender(EventPostEntityRender event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        positionMap.clear();
        for (Entity entity : event.entities()) {
            if (entity == null) continue;

            Rect rect = projEntity(entity);

            if (rect != null) {
                positionMap.put(entity, rect);
            }
        }
    }

    public Vec3 project(double x, double y, double z) {
        return Projection.w2s((float) x, (float) y, (float) z);
    }

    public Rect getOrPut(Entity entity) {
        Rect rect = projEntity(entity);
        if (rect != null) {
            positionMap.put(entity, rect);
        }
        return positionMap.get(entity);
    }

    public Map<Entity, Rect> getPositionMap() {
        return positionMap;
    }

    public Rect getScreenPosition(Entity entity) {
        return positionMap.get(entity);
    }
}