package eu.shoroa.ross.feature.module.impl.player;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.event.EventRender3D;
import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.Bind;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

import static eu.shoroa.ross.Client.mc;

public class ModuleFreeCam extends Module {
    private static final int DUMMY_ENTITY_ID = -6969;

    private final SettingCategory settings = addCategory("Settings", ".", "settings");
    private final NumberSetting speed = register(new NumberSetting("Speed", "speed", 1f, 0.5f, 6f, 0.1f), settings);
    private final BooleanSetting sneak = register(new BooleanSetting("Sneak Player", "sneak", true), settings);

    private EntityOtherPlayerMP dummy;

    private double startX, startY, startZ;
    private long lastMoveNano;

    public ModuleFreeCam() {
        super("Free Cam", "View the world freely without moving your body.", Category.PLAYER,
                new Bind(Keyboard.KEY_V, EventInput.Type.KEYBOARD, EventInput.Action.PRESS), "\ue741");
    }

    public EntityOtherPlayerMP getDummy() {
        return dummy;
    }

    public boolean getSneak() {
        return sneak.get();
    }

    @Override
    public void onEnable() {
        EntityPlayerSP player = mc.thePlayer;
        if (player == null || mc.theWorld == null) {
            setEnabled(false);
            return;
        }
        super.onEnable();

        startX = player.posX;
        startY = player.posY;
        startZ = player.posZ;
        lastMoveNano = System.nanoTime();

        dummy = new EntityOtherPlayerMP(mc.theWorld, player.getGameProfile());
        dummy.copyLocationAndAnglesFrom(player);
        dummy.rotationYawHead = player.rotationYawHead;
        dummy.renderYawOffset = player.renderYawOffset;
        dummy.inventory.copyInventory(player.inventory);
        dummy.prevPosX = dummy.posX;
        dummy.prevPosY = dummy.posY;
        dummy.prevPosZ = dummy.posZ;

        mc.theWorld.addEntityToWorld(DUMMY_ENTITY_ID, dummy);
        mc.setRenderViewEntity(dummy);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        EntityPlayerSP player = mc.thePlayer;
        if (player != null) {
            if (mc.getRenderViewEntity() == dummy || mc.getRenderViewEntity() == null) {
                mc.setRenderViewEntity(player);
            }
            if (sneak.get() && player.isSneaking()) {
                player.setSneaking(false);
            }
        }

        if (dummy != null && mc.theWorld != null) {
            mc.theWorld.removeEntityFromWorld(DUMMY_ENTITY_ID);
        }
        dummy = null;
    }

    @Subscribe
    public void onTick(EventTick event) {
        checkAllowed();
    }

    private void checkAllowed() {
        if (mc.thePlayer == null || mc.theWorld == null || dummy == null) {
            setEnabled(false);
            return;
        }

        double drift = 0.01;
        boolean moved = Math.abs(mc.thePlayer.posX - startX) > drift
                || Math.abs(mc.thePlayer.posY - startY) > drift
                || Math.abs(mc.thePlayer.posZ - startZ) > drift;

        String reason = null;
        if (mc.thePlayer.hurtTime > 0 && !mc.thePlayer.isBurning()) {
            reason = "Took combat damage";
        } else if (mc.thePlayer.isRiding()) {
            reason = "Riding an entity";
        } else if (mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) {
            reason = "Entered fluid";
        } else if (!mc.thePlayer.onGround && !mc.thePlayer.capabilities.isFlying) {
            reason = "Left the ground";
        } else if (mc.thePlayer.isDead) {
            reason = "Player died";
        } else if (mc.thePlayer.isOnLadder()) {
            reason = "Climbing";
        } else if (moved) {
            reason = "Position changed";
        }

        if (reason != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§c[Free Cam] Disabled: §7" + reason));
            setEnabled(false);
        }
    }

    @Subscribe
    public void onRender3D(EventRender3D event) {
        if (mc.thePlayer == null || dummy == null) return;

        long now = System.nanoTime();
        double delta = Math.min((now - lastMoveNano) / 1.0e9, 0.1);
        lastMoveNano = now;

        dummy.prevPosX = dummy.posX;
        dummy.prevPosY = dummy.posY;
        dummy.prevPosZ = dummy.posZ;

        double step = speed.get() * delta * 20.0;

        float forward = 0f;
        float strafe = 0f;
        if (mc.gameSettings.keyBindForward.isKeyDown()) forward += 1f;
        if (mc.gameSettings.keyBindBack.isKeyDown()) forward -= 1f;
        if (mc.gameSettings.keyBindLeft.isKeyDown()) strafe += 1f;
        if (mc.gameSettings.keyBindRight.isKeyDown()) strafe -= 1f;

        float yaw = dummy.rotationYaw;
        double dx = 0, dy = 0, dz = 0;

        if (forward != 0f || strafe != 0f) {
            if (forward != 0f) {
                if (strafe > 0f) {
                    yaw += forward > 0f ? -45f : 45f;
                } else if (strafe < 0f) {
                    yaw += forward > 0f ? 45f : -45f;
                }
                strafe = 0f;
                forward = forward > 0f ? 1f : -1f;
            }

            double radians = Math.toRadians(yaw);
            dx = forward * step * -Math.sin(radians) + strafe * step * Math.cos(radians);
            dz = forward * step * Math.cos(radians) + strafe * step * Math.sin(radians);
        }

        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            dy = step;
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            dy = -step;
        }

        dummy.setPosition(dummy.posX + dx, dummy.posY + dy, dummy.posZ + dz);

        dummy.prevPosX = dummy.posX;
        dummy.prevPosY = dummy.posY;
        dummy.prevPosZ = dummy.posZ;
    }
}
