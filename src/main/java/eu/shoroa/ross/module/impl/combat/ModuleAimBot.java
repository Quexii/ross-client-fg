package eu.shoroa.ross.module.impl.combat;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.event.*;
import eu.shoroa.ross.mixins.injection.client.MinecraftAccessor;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.render.Renderer;
import eu.shoroa.ross.settings.BooleanSetting;
import eu.shoroa.ross.settings.NumberSetting;
import io.github.humbleui.skija.Canvas;
import io.github.humbleui.skija.Paint;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.Display;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static eu.shoroa.ross.Client.mc;

public class ModuleAimBot extends Module {
    private final NumberSetting fov = register(new NumberSetting("FOV", "aimbot.fov", 90, 0, 360, 1));
    private final NumberSetting range = register(new NumberSetting("Range", "aimbot.range", 4.2f, 0, 10, 0.1f));
    private final NumberSetting speed = register(new NumberSetting("Speed", "aimbot.speed", 10f, 1f, 100f, 1f));
    private final BooleanSetting render = register(new BooleanSetting("Render", "aimbot.render", true));
    private final BooleanSetting playersOnly = register(new BooleanSetting("Players Only", "aimbot.players", true));

    private EntityLivingBase target;

    public ModuleAimBot() {
        super("AimBot", "Aims at nearby entities", Category.COMBAT);
    }

    @Subscribe
    public void onMotion(EventMotion.Post event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        findTarget();

        if (target != null) {
            float neededYaw = getNeededYaw(target);
            float neededPitch  = getNeededPitch(target);
            float yawDiff = getYawDiff(neededYaw);
            float pitchDiff = Math.abs(neededPitch - mc.thePlayer.rotationPitch);

            if (yawDiff > 1e-5) {
                float maxChange = speed.get().floatValue() * event.partialTicks * 20f;
                float change = Math.min(maxChange, yawDiff);
                if (MathHelper.wrapAngleTo180_float(neededYaw - mc.thePlayer.rotationYaw) < 0) {
                    change = -change;
                }
                mc.thePlayer.rotationYaw += change;
            }

            if (pitchDiff > 1e-5) {
                float maxChange = speed.get().floatValue() * event.partialTicks * 20f;
                float change = Math.min(maxChange, pitchDiff);
                if (neededPitch - mc.thePlayer.rotationPitch < 0) {
                    change = -change;
                }
                mc.thePlayer.rotationPitch += change;
            }
        }
    }

    @Subscribe
    public void onRender(EventHUD.TopSkia event) {
        if (!render.get()) return;

        Canvas canvas = Client.INSTANCE.skia.getCanvas();
        try (Paint p = new Paint()) {
            float centerX = Display.getWidth() / 2f;
            float centerY = Display.getHeight() / 2f;
            float radius = (fov.get().floatValue() / 180f) * (Display.getWidth() / 2f);

            p.setStroke(true);
            p.setStrokeWidth(2f);
            p.setColor(0xaaFFFFFF);
            canvas.drawCircle(centerX, centerY, radius, p);

            if (target != null) {
                float neededYaw = getNeededYaw(target);
                float neededPitch = getNeededPitch(target);
                float yawDelta = MathHelper.wrapAngleTo180_float(neededYaw - mc.thePlayer.rotationYaw);
                float pitchDelta = neededPitch - mc.thePlayer.rotationPitch;
                float pixelsPerDegree = radius / (fov.get().floatValue() * 0.5f);

                float dx = yawDelta * pixelsPerDegree;
                float dy = -pitchDelta * pixelsPerDegree;
                float len = (float) Math.sqrt(dx * dx + dy * dy);
                if (len > radius && len > 1e-4f) {
                    float scale = radius / len;
                    dx *= scale;
                    dy *= scale;
                }

                p.setStroke(false);
                canvas.drawLine(centerX, centerY, centerX + dx, centerY - dy, p);
            }
        }
    }

    private void findTarget() {
        List<EntityLivingBase> entities = mc.theWorld.loadedEntityList.stream()
                .filter(e -> e instanceof EntityLivingBase)
                .map(e -> (EntityLivingBase) e)
                .filter(e -> e != mc.thePlayer)
                .filter(e -> !playersOnly.get() || e instanceof EntityPlayer)
                .filter(e -> e.deathTime == 0)
                .filter(e -> !e.isDead)
                .filter(e -> mc.thePlayer.getDistanceToEntity(e) <= range.get())
                .filter(e -> getYawDiff(getNeededYaw(e)) <= fov.get() / 2f)
                .sorted(Comparator.comparingDouble(e -> getYawDiff(getNeededYaw(e))))
                .collect(Collectors.toList());

        target = entities.isEmpty() ? null : entities.get(0);
    }

    private float getNeededYaw(EntityLivingBase entity) {
        double deltaX = entity.posX - mc.thePlayer.posX;
        double deltaZ = entity.posZ - mc.thePlayer.posZ;
        return (float) Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90f;
    }

    private float getNeededPitch(EntityLivingBase entity) {
        double deltaX = entity.posX - mc.thePlayer.posX;
        double deltaZ = entity.posZ - mc.thePlayer.posZ;
        double deltaY = (entity.posY + entity.getEyeHeight()) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        return (float) -Math.toDegrees(Math.atan2(deltaY, distanceXZ));
    }

    private float getYawDiff(float yaw) {
        return Math.abs(MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        target = null;
    }
}