package eu.shoroa.ross.feature.module.impl.misc;

import eu.shoroa.ross.event.EventRender3D;
import eu.shoroa.ross.event.EventRenderEntities;
import eu.shoroa.ross.event.Hud;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.render.opengl.Renderer3D;
import eu.shoroa.ross.render.ui.Align;
import eu.shoroa.ross.render.ui.Fonts;
import eu.shoroa.ross.render.ui.UI;
import eu.shoroa.ross.utils.proj.EntityProjection;
import io.github.humbleui.skija.Paint;
import io.github.humbleui.skija.Path;
import io.github.humbleui.types.Point;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

import static eu.shoroa.ross.Client.mc;

public class ModuleFireballWarning extends Module {
    private final SettingCategory settings = addCategory("Settings", "settings", "settings");
    private final NumberSetting lineLength = register(new NumberSetting("Line Length", "line_length", 50, 1, 100, 1), settings);

    private final Map<EntityFireball, Vec3> positions = new HashMap<>();

    public ModuleFireballWarning() {
        super("Fireball Warning", "Warns about incoming fireballs", Category.MISC, "\uf685");
    }

    @Subscribe
    @ApiStatus.Internal
    public void onRenderEntitiesPost(EventRenderEntities.Post event) {
        EntityFireball[] entities = mc.theWorld.getLoadedEntityList().stream().filter(entity -> entity instanceof EntityFireball).toArray(EntityFireball[]::new);
        positions.clear();

        double renderX = mc.getRenderManager().viewerPosX;
        double renderY = mc.getRenderManager().viewerPosY;
        double renderZ = mc.getRenderManager().viewerPosZ;

        for (EntityFireball entity : entities) {
            double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks;
            double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks + entity.height * 0.5;
            double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks;

            positions.put(entity, EntityProjection.getInstance().project(x - renderX, y - renderY, z - renderZ));
        }
    }

    @Subscribe
    public void onRender3D(EventRender3D event) {
        EntityFireball[] entities = mc.theWorld.getLoadedEntityList().stream().filter(entity -> entity instanceof EntityFireball).toArray(EntityFireball[]::new);

        if (entities.length == 0) return;

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

    @Subscribe
    @ApiStatus.Internal
    public void onHud(Hud.Layer event) {
        if (event.is(Hud.Layer.NAME_SKIA_TOP) && mc.thePlayer != null && mc.theWorld != null) {
            positions.forEach(this::drawMarker);
        }
    }

    private void drawMarker(EntityFireball entity, Vec3 pos) {
        float sw = mc.displayWidth;
        float sh = mc.displayHeight;
        float edgePad = 42f;

        boolean onScreen = pos != null
                && pos.xCoord >= edgePad && pos.xCoord <= sw - edgePad
                && pos.yCoord >= edgePad && pos.yCoord <= sh - edgePad;

        float distance = mc.thePlayer.getDistanceToEntity(entity);

        if (onScreen) {
            drawOnScreenMarker((float) pos.xCoord, (float) pos.yCoord, distance);
        } else {
            // Projection is unusable (behind camera) or off-screen: clamp to the
            // screen border and point an arrow toward the fireball.
            float cx = sw / 2f, cy = sh / 2f;
            float dirX, dirY;

            if (pos != null) {
                dirX = (float) pos.xCoord - cx;
                dirY = (float) pos.yCoord - cy;
            } else {
                Vec3 dir = screenDirection(entity);
                dirX = (float) dir.xCoord;
                dirY = (float) dir.yCoord;
            }

            float len = MathHelper.sqrt_float(dirX * dirX + dirY * dirY);
            if (len < 1.0E-4f) {
                dirX = 0f;
                dirY = 1f;
            } else {
                dirX /= len;
                dirY /= len;
            }

            // Distance from center to the padded screen border along the direction.
            float tx = dirX > 0 ? (sw - edgePad - cx) / dirX : dirX < 0 ? (edgePad - cx) / dirX : Float.MAX_VALUE;
            float ty = dirY > 0 ? (sh - edgePad - cy) / dirY : dirY < 0 ? (edgePad - cy) / dirY : Float.MAX_VALUE;
            float t = Math.min(tx, ty);

            drawEdgeMarker(cx + dirX * t, cy + dirY * t, dirX, dirY, distance);
        }
    }

    /**
     * Screen-space direction toward the entity built from the camera basis
     * (yaw/pitch), so it stays correct even when gluProject rejects the point.
     */
    private Vec3 screenDirection(EntityFireball entity) {
        RenderManager rm = mc.getRenderManager();
        double dx = entity.posX - rm.viewerPosX;
        double dy = entity.posY - (rm.viewerPosY + mc.thePlayer.getEyeHeight());
        double dz = entity.posZ - rm.viewerPosZ;

        float yaw = (float) Math.toRadians(rm.playerViewY);
        float pitch = (float) Math.toRadians(rm.playerViewX);
        float sinYaw = MathHelper.sin(yaw), cosYaw = MathHelper.cos(yaw);
        float sinPitch = MathHelper.sin(pitch), cosPitch = MathHelper.cos(pitch);

        // Camera right and up vectors; screen y grows downward.
        double screenX = dx * -cosYaw + dz * -sinYaw;
        double screenY = -(dx * -sinYaw * sinPitch + dy * cosPitch + dz * cosYaw * sinPitch);

        return new Vec3(screenX, screenY, 0);
    }

    private void drawOnScreenMarker(float px, float py, float distance) {
        StellaTheme t = StellaTheme.get();
        float r = 15f;
        float cxm = px;
        float cym = py - r - 19f;

        try (Paint p = new Paint()) {
            drawPulse(cxm, cym, r, p);

            // Chevron pointing down at the fireball.
            try (Path tail = Path.makePolygon(new Point[]{
                    new Point(cxm, py - 8f),
                    new Point(cxm - 6f, cym + r - 2f),
                    new Point(cxm + 6f, cym + r - 2f)
            }, true)) {
                p.setColor(t.accent);
                UI.drawPath(tail, p);
            }

            drawBadge(cxm, cym, r, p);
            drawDistance(cxm, cym - r - 12f, distance, p);
        }
    }

    private void drawEdgeMarker(float mx, float my, float dirX, float dirY, float distance) {
        StellaTheme t = StellaTheme.get();
        float r = 15f;

        try (Paint p = new Paint()) {
            drawPulse(mx, my, r, p);

            // Arrow on the outside of the badge, pointing off-screen.
            UI.save();
            UI.translate(mx, my);
            UI.rotate((float) Math.toDegrees(Math.atan2(dirY, dirX)));
            try (Path arrow = Path.makePolygon(new Point[]{
                    new Point(r + 13f, 0f),
                    new Point(r + 3f, -7f),
                    new Point(r + 3f, 7f)
            }, true)) {
                p.setColor(t.accent);
                UI.drawPath(arrow, p);
                p.setStroke(true);
                p.setStrokeWidth(1.5f);
                p.setColor(t.outline);
                UI.drawPath(arrow, p);
                p.setStroke(false);
            }
            UI.restore();

            drawBadge(mx, my, r, p);
            // Distance label sits toward the screen center so it never clips.
            drawDistance(mx - dirX * (r + 16f), my - dirY * (r + 16f), distance, p);
        }
    }

    private void drawPulse(float x, float y, float r, Paint p) {
        StellaTheme t = StellaTheme.get();
        float pulse = (System.currentTimeMillis() % 1200L) / 1200f;

        p.setStroke(true);
        p.setStrokeWidth(2.5f);
        p.setColor(withAlpha(t.accent, (1f - pulse) * 0.65f));
        UI.drawCircle(x, y, r + 3f + pulse * 9f, p);
        p.setStroke(false);
    }

    private void drawBadge(float x, float y, float r, Paint p) {
        StellaTheme t = StellaTheme.get();

        p.setColor(t.shadow);
        UI.drawCircle(x, y + 2f, r, p);

        p.setColor(t.surface);
        UI.drawCircle(x, y, r, p);

        p.setStroke(true);
        p.setStrokeWidth(3f);
        p.setColor(t.accent);
        UI.drawCircle(x, y, r, p);
        p.setStroke(false);

        p.setColor(t.foreground);
        UI.drawText("\uf685", x, y, Fonts.MaterialIcons.weight(400).fill(true), 19f, Align.CENTER, p);
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

    private static int withAlpha(int color, float alpha) {
        int a = (int) (((color >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, alpha)));
        return (a << 24) | (color & 0xFFFFFF);
    }
}
