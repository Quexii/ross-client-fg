package eu.shoroa.ross.feature.module.impl.player;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.module.Bind;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.BooleanSetting;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import static eu.shoroa.ross.Client.mc;

public class ModuleFreeLook extends Module {
    private final SettingCategory settings = addCategory("Settings", ".", "settings");
    private final NumberSetting sensitivity = register(new NumberSetting("Sensitivity", "sensitivity", 1f, 0.1f, 3f, 0.1f), settings);
    private final BooleanSetting invertX = register(new BooleanSetting("Invert X", "invert_x", false), settings);
    private final BooleanSetting invertY = register(new BooleanSetting("Invert Y", "invert_y", true), settings);

    private int prevPerspective = 0;
    private float yaw = 0f;
    private float pitch = 0f;

    public ModuleFreeLook() {
        super("Free Look", "Allows you to look around freely.", Category.MISC, new Bind(Keyboard.KEY_X, EventInput.Type.KEYBOARD, EventInput.Action.HOLD), MaterialIcons._3D_ROTATION);
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