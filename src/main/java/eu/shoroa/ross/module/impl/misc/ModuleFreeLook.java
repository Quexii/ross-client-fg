package eu.shoroa.ross.module.impl.misc;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.module.Bind;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.settings.BooleanSetting;
import eu.shoroa.ross.settings.NumberSetting;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import static eu.shoroa.ross.Client.mc;

public class ModuleFreeLook extends Module {

    private final NumberSetting sensitivity = register(new NumberSetting("Sensitivity", 1f, 0.1f, 3f, 0.1f));
    private final BooleanSetting invertX = register(new BooleanSetting("Invert X", false));
    private final BooleanSetting invertY = register(new BooleanSetting("Invert Y", false));

    private int prevPerspective = 0;
    private float yaw = 0f;
    private float pitch = 0f;

    public ModuleFreeLook() {
        super("FreeLook", "Allows you to look around freely", Category.MISC, new Bind(Keyboard.KEY_X, EventInput.Type.KEYBOARD, EventInput.Action.HOLD));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        prevPerspective = mc.gameSettings.thirdPersonView;
        mc.gameSettings.thirdPersonView = 1;

        yaw = mc.thePlayer.rotationYaw;
        pitch = mc.thePlayer.rotationPitch;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        mc.gameSettings.thirdPersonView = prevPerspective;
    }

    @SubscribeEvent
    public void oe$CameraSetup(EntityViewRenderEvent.CameraSetup event) {
//        event.yaw = yaw + 180F;
//        event.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean applyMouseDelta() {
        if (mc.inGameHasFocus && Display.isActive()) {
            if (!isEnabled()) return true;

            mc.mouseHelper.mouseXYChange();
            float rawDX = mc.mouseHelper.deltaX;
            float rawDY = mc.mouseHelper.deltaY;

            float s = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
            float scale = s * s * s * 8f * 0.15f * sensitivity.get();

            int invertMouse = mc.gameSettings.invertMouse ? -1 : 1;
            yaw += (invertX.get() ? -rawDX : rawDX) * scale * invertMouse;
            pitch += (invertY.get() ? -rawDY : rawDY) * scale * invertMouse;

            if (pitch > 90) pitch = 90;
            if (pitch < -90) pitch = -90;
        }

        return false;
    }
}
