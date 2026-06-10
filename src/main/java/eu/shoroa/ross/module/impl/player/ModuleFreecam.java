package eu.shoroa.ross.module.impl.player;

import com.mojang.authlib.GameProfile;
import eu.shoroa.ross.event.EventRender3D;
import eu.shoroa.ross.event.EventSelfUpdate;
import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.settings.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.lwjgl.opengl.Display;

import static eu.shoroa.ross.Client.mc;

public class ModuleFreecam extends Module {
    private double camX, camY, camZ;
    private double prevCamX, prevCamY, prevCamZ;
    private float camYaw, camPitch;

    private final FakeMovement fakeMovement = new FakeMovement();
    private MovementInput previousMovementInput;
    private int oldPerspective = 0;

    private final NumberSetting speedSetting = register(new NumberSetting("Speed", "freecam_speed", 1, 0.1f, 5, 0.1f));

    public ModuleFreecam() {
        super("Freecam", "Allows you to fly around with your camera.", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        camX = mc.thePlayer.posX;
        camY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
        camZ = mc.thePlayer.posZ;

        prevCamX = mc.thePlayer.lastTickPosX;
        prevCamY = mc.thePlayer.lastTickPosY + mc.thePlayer.getEyeHeight();
        prevCamZ = mc.thePlayer.lastTickPosZ;

        camYaw = mc.thePlayer.rotationYaw;
        camPitch = mc.thePlayer.rotationPitch;

        previousMovementInput = mc.thePlayer.movementInput;
        mc.thePlayer.movementInput = fakeMovement;
        oldPerspective = mc.gameSettings.thirdPersonView;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        mc.thePlayer.movementInput = previousMovementInput;
        mc.gameSettings.thirdPersonView = oldPerspective;
    }

    @Subscribe
    public void oe$Tick(EventSelfUpdate event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        mc.gameSettings.thirdPersonView = 1;

        prevCamX = camX;
        prevCamY = camY;
        prevCamZ = camZ;

        mc.thePlayer.movementInput.moveForward = 0f;
        mc.thePlayer.movementInput.moveStrafe = 0f;
        mc.thePlayer.movementInput.jump = false;
        mc.thePlayer.movementInput.sneak = false;

        float forward = 0.0F;
        float strafe = 0.0F;

        if (mc.gameSettings.keyBindForward.isKeyDown()) forward += 1.0F;
        if (mc.gameSettings.keyBindBack.isKeyDown()) forward -= 1.0F;
        if (mc.gameSettings.keyBindLeft.isKeyDown()) strafe += 1.0F;
        if (mc.gameSettings.keyBindRight.isKeyDown()) strafe -= 1.0F;

        double speed = speedSetting.get();
        double rad = Math.toRadians(camYaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        camX += (forward * -sin + strafe * cos) * speed;
        camZ += (forward * cos + strafe * sin) * speed;

        if (mc.gameSettings.keyBindJump.isKeyDown()) camY += speed;
        if (mc.gameSettings.keyBindSneak.isKeyDown()) camY -= speed;
    }

    public boolean applyMouseDelta() {
        if (mc.inGameHasFocus && Display.isActive()) {
            if (!isEnabled()) return true;

            mc.mouseHelper.mouseXYChange();
            float rawDX = mc.mouseHelper.deltaX;
            float rawDY = mc.mouseHelper.deltaY;
            float s = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
            float scale = s * s * s * 8.0F * 0.15f;
            float yaw = rawDX * scale;
            float pitch = rawDY * scale;
            int invertMouse = mc.gameSettings.invertMouse ? -1 : 1;

            camYaw += yaw;
            camPitch -= pitch * invertMouse;

            camYaw %= 360;
            if (camPitch > 90) camPitch = 90;
            if (camPitch < -90) camPitch = -90;
        }

        return false;
    }

    public void orientCamera(float partialTicks) {
        double pX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
        double pY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks;
        double pZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;

        double cX = prevCamX + (camX - prevCamX) * partialTicks;
        double cY = prevCamY + (camY - prevCamY) * partialTicks;
        double cZ = prevCamZ + (camZ - prevCamZ) * partialTicks;

        GlStateManager.rotate(camPitch, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(camYaw + 180.0F, 0.0F, 1.0F, 0.0F);

        GlStateManager.translate(pX - cX, pY - cY, pZ - cZ);
    }

    private class FakeMovement extends MovementInput {
        @Override
        public void updatePlayerMoveState() {

        }
    }

    public double getCamX() {
        return camX;
    }

    public double getCamY() {
        return camY;
    }

    public double getCamZ() {
        return camZ;
    }

    public double getPrevCamX() {
        return prevCamX;
    }

    public double getPrevCamY() {
        return prevCamY;
    }

    public double getPrevCamZ() {
        return prevCamZ;
    }

    public float getCamYaw() {
        return camYaw;
    }

    public float getCamPitch() {
        return camPitch;
    }

    public double getRenderX(float partialTicks) {
        return prevCamX + (camX - prevCamX) * partialTicks;
    }

    public double getRenderY(float partialTicks) {
        return prevCamY + (camY - prevCamY) * partialTicks;
    }

    public double getRenderZ(float partialTicks) {
        return prevCamZ + (camZ - prevCamZ) * partialTicks;
    }
}
