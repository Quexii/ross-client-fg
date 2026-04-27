package eu.shoroa.ross.module.impl.misc;

import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.EventPostEntityRender;
import eu.shoroa.ross.module.Bind;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.settings.NumberSetting;
import eu.shoroa.ross.types.Rect;
import eu.shoroa.ross.util.proj.EntityProjection;
import eu.shoroa.ross.util.render.Renderer3D;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

import java.util.HashMap;
import java.util.Map;

import static eu.shoroa.ross.Client.mc;

public class ModuleFireballWarning extends Module {
    private final NumberSetting lineLength = register(new NumberSetting("Line Length", 50, 1, 100, 1));

    private final Map<EntityFireball, Vec3> positions = new HashMap<>();

    public ModuleFireballWarning() {
        super("Fireball Warning", "Warns you about incoming fireballs", Category.MISC, null);
    }


    @SubscribeEvent
    public void oe$PostEntityRender(EventPostEntityRender event) {
        EntityFireball[] entities = mc.theWorld.getLoadedEntityList().stream().filter(entity -> entity instanceof EntityFireball).toArray(EntityFireball[]::new);
        positions.clear();

        double renderX = mc.getRenderManager().viewerPosX;
        double renderY = mc.getRenderManager().viewerPosY;
        double renderZ = mc.getRenderManager().viewerPosZ;

        for (EntityFireball entity : entities) {
            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks();
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks() + entity.height * 0.5;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks();

            positions.put(entity, EntityProjection.getInstance().project(x - renderX, y - renderY, z - renderZ));
        }
    }

    @SubscribeEvent
    public void oe$Render3D(RenderWorldLastEvent event) {
        EntityFireball[] entities = mc.theWorld.getLoadedEntityList().stream().filter(entity -> entity instanceof EntityFireball).toArray(EntityFireball[]::new);

        final float ll = lineLength.get() * 2;

        double renderX = mc.getRenderManager().viewerPosX;
        double renderY = mc.getRenderManager().viewerPosY;
        double renderZ = mc.getRenderManager().viewerPosZ;

        Renderer3D.begin3D(2f);
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
        for (EntityFireball entity : entities) {
            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks + entity.height * 0.5;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks;

            Vec3 startPos = new Vec3(
                    x - renderX,
                    y - renderY,
                    z - renderZ
            );


            Vec3 endPos = new Vec3(
                    x + entity.motionX * ll - renderX,
                    y + entity.motionY * ll - renderY,
                    z + entity.motionZ * ll - renderZ
            );

            Renderer3D.drawLine(startPos, endPos, 0xFFFF0000);
        }
        Renderer3D.end3D();
    }

    @SubscribeEvent
    public void oe$SkiaBottom(EventHUD.BottomSkia event) {
        EntityFireball[] entities = mc.theWorld.getLoadedEntityList().stream().filter(entity -> entity instanceof EntityFireball).toArray(EntityFireball[]::new);

        float screenWidth = Display.getWidth();
        float screenHeight = Display.getHeight();

        float cx = screenWidth / 2;
        float cy = screenHeight / 2;

        for (EntityFireball entity : entities) {
            Vec3 pos = positions.get(entity);

            if (pos == null || pos.zCoord <= 0) continue;

            float rx = (float) pos.xCoord;
            float ry = (float) pos.yCoord;

            float dx = rx - cx;
            float dy = ry - cy;

            float mx = screenWidth / 2f - 10f;
            float my = screenHeight / 2f - 10f;

            float nx = dx / mx;
            float ny = dy / my;

            float len = (float) Math.sqrt(nx * nx + ny * ny);

            if (len > 1f) {
                nx /= len;
                ny /= len;

                rx = cx + nx * mx;
                ry = cy + ny * my;
            }

            try (Paint p = new Paint()) {
                p.setColor(0xFFFF0000);
                Renderer.drawRect(rx - 8, ry - 8, 16, 16, p);
            }
        }
    }
}
