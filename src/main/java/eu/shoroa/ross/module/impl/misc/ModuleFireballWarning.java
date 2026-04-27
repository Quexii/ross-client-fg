package eu.shoroa.ross.module.impl.misc;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.EventHUD;
import eu.shoroa.ross.event.EventPostEntityRender;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.mixins.injection.client.MinecraftAccessor;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.render.skia.font.Font;
import eu.shoroa.ross.render.skia.font.Fonts;
import eu.shoroa.ross.render.skia.font.VariableFont;
import eu.shoroa.ross.settings.BooleanSetting;
import eu.shoroa.ross.settings.NumberSetting;
import eu.shoroa.ross.util.proj.EntityProjection;
import eu.shoroa.ross.util.render.Renderer3D;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.Display;

import java.util.HashMap;
import java.util.Map;

import static eu.shoroa.ross.Client.mc;

public class ModuleFireballWarning extends Module {
    private static final float EDGE_MARGIN = 10f;
    private final NumberSetting lineLength = register(new NumberSetting("Line Length", "fireballwarning.line_length", 50, 1, 100, 1));
    private final NumberSetting markerSize = register(new NumberSetting("Marker Size", "fireballwarning.marker_size", 18, 10, 40, 1));
    private final BooleanSetting markerPulse = register(new BooleanSetting("Marker Pulse", "fireballwarning.marker_pulse", true));

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
        if (mc.theWorld == null || mc.thePlayer == null) return;

        EntityFireball[] entities = mc.theWorld.getLoadedEntityList().stream().filter(entity -> entity instanceof EntityFireball).toArray(EntityFireball[]::new);
        float partialTicks = ((MinecraftAccessor) mc).getTimer().renderPartialTicks;

        float screenWidth = Display.getWidth();
        float screenHeight = Display.getHeight();

        float cx = screenWidth / 2;
        float cy = screenHeight / 2;

        for (EntityFireball entity : entities) {
            Vec3 pos = positions.get(entity);

            float rx;
            float ry;

            if (pos != null && pos.zCoord > 0) {
                rx = (float) pos.xCoord;
                ry = (float) pos.yCoord;
            } else {
                Vec3 fallbackPos = projectFallback(entity, partialTicks, screenWidth, screenHeight);
                if (fallbackPos == null) continue;

                rx = (float) fallbackPos.xCoord;
                ry = (float) fallbackPos.yCoord;
            }

            float dx = rx - cx;
            float dy = ry - cy;

            float mx = screenWidth / 2f - EDGE_MARGIN;
            float my = screenHeight / 2f - EDGE_MARGIN;

            float nx = dx / mx;
            float ny = dy / my;

            float len = (float) Math.sqrt(nx * nx + ny * ny);

            if (len > 1f) {
                nx /= len;
                ny /= len;

                rx = cx + nx * mx;
                ry = cy + ny * my;
            }

            drawWarningMarker(rx, ry);
        }
    }

    private void drawWarningMarker(float x, float y) {
        Canvas canvas = Client.INSTANCE.skia.getCanvas();
        if (canvas == null) return;

        float baseSize = markerSize.get();
        float pulse = markerPulse.get() ? 1f + (float) Math.sin(System.currentTimeMillis() * 0.0125) * 0.09f : 1f;
        float outerSize = baseSize * pulse;
        float innerSize = outerSize * 0.62f;

        canvas.save();
        canvas.translate(x, y);
        canvas.rotate(45f);

        try (Paint p = new Paint()) {
            p.setAntiAlias(true);
            p.setColor(0x44220000);
            Renderer.drawRRect(-outerSize * 0.62f, -outerSize * 0.62f, outerSize * 1.24f, outerSize * 1.24f, 5f, p);
        }

        try (Paint p = new Paint()) {
            p.setAntiAlias(true);
            p.setColor(0xFFE53935);
            Renderer.drawRRect(-outerSize / 2f, -outerSize / 2f, outerSize, outerSize, 4f, p);

            p.setStroke(true);
            p.setStrokeWidth(2f);
            p.setColor(0xFFFFD7D7);
            Renderer.drawRRect(-outerSize / 2f, -outerSize / 2f, outerSize, outerSize, 4f, p);
        }

        try (Paint p = new Paint()) {
            p.setAntiAlias(true);
            p.setColor(0xFF4A0909);
            Renderer.drawRRect(-innerSize / 2f, -innerSize / 2f, innerSize, innerSize, 3f, p);
        }

        canvas.restore();

        VariableFont.DerivedFont font = Fonts.GoogleFlex
                .weight(760)
                .roundness(0)
                .opticSize(24);

        try (Paint p = new Paint()) {
            p.setAntiAlias(true);
            p.setColor(0xFFFFF3F3);
            Renderer.drawText("!", x, y + 0.5f, font, Math.max(13f, baseSize * 0.85f), Font.Align.CENTER, p);
        }
    }

    private Vec3 projectFallback(EntityFireball entity, float partialTicks, float screenWidth, float screenHeight) {
        Entity camera = mc.getRenderViewEntity();
        if (camera == null) return null;

        double renderX = mc.getRenderManager().viewerPosX;
        double renderY = mc.getRenderManager().viewerPosY;
        double renderZ = mc.getRenderManager().viewerPosZ;

        double worldX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double worldY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks + entity.height * 0.5;
        double worldZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;

        double x = worldX - renderX;
        double y = worldY - renderY;
        double z = worldZ - renderZ;

        double yaw = Math.toRadians(camera.rotationYaw);
        double pitch = Math.toRadians(camera.rotationPitch);

        double sinYaw = Math.sin(yaw);
        double cosYaw = Math.cos(yaw);
        double sinPitch = Math.sin(pitch);
        double cosPitch = Math.cos(pitch);

        double camX = x * cosYaw - z * sinYaw;
        double camZ = z * cosYaw + x * sinYaw;
        double camY = y * cosPitch - camZ * sinPitch;
        camZ = y * sinPitch + camZ * cosPitch;

        if (camZ < 0) {
            camX = -camX;
            camY = -camY;
        }

        double dirX = camX;
        double dirY = -camY;
        double length = Math.sqrt(dirX * dirX + dirY * dirY);

        if (length < 1.0E-4) {
            dirX = 0;
            dirY = -1;
            length = 1;
        }

        float cx = screenWidth / 2f;
        float cy = screenHeight / 2f;
        float mx = cx - EDGE_MARGIN;
        float my = cy - EDGE_MARGIN;

        float rx = cx + (float) (dirX / length) * mx;
        float ry = cy + (float) (dirY / length) * my;
        return new Vec3(rx, ry, 1);
    }
}
