package eu.shoroa.ross.feature.module.impl.combat;

import eu.shoroa.ross.event.EventMotion;
import eu.shoroa.ross.event.EventPacket;
import eu.shoroa.ross.event.EventSelfUpdate;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.ModeEnum;
import eu.shoroa.ross.feature.setting.ModeSetting;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.mixins.interfaces.IKeyBinding;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraft.network.play.server.S12PacketEntityVelocity;

import static eu.shoroa.ross.Client.mc;

public class ModuleVelocity extends Module {
    private final SettingCategory categorySettings = addCategory("Settings", ".", "settings");
    private final ModeSetting<Mode> modeSetting = register(new ModeSetting<>("Mode", "mode", Mode.PERCENT), categorySettings);
    private final NumberSetting horizontalPercent = register(new NumberSetting("Horizontal Percent", "horizontal_percent", 100, 0, 100, 1), categorySettings);
    private final NumberSetting verticalPercent = register(new NumberSetting("Vertical Percent", "vertical_percent", 100, 0, 100, 1), categorySettings);

    private int velocityTimer = 100;
    private boolean isValidHit = false;
    private boolean jumped = false;

    public ModuleVelocity() {
        super("Velocity", "Reduces your knockback", Category.COMBAT, MaterialIcons.ARROW_DOWNWARD);
    }

    @Subscribe
    public void oe$PreMotion(EventMotion.Pre event) {

    }

    @Subscribe
    public void oe$Update(EventSelfUpdate event) {
        if (mc.thePlayer == null) return;

        velocityTimer++;

        if (mc.thePlayer.hurtTime == 10) {
            isValidHit = (velocityTimer <= 5);
        }

        boolean wPressed = mc.gameSettings.keyBindForward.getKeyCode() < 0 ? Mouse.isButtonDown(mc.gameSettings.keyBindForward.getKeyCode() + 100) : Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());

        if (modeSetting.get().equals(Mode.LEGIT_W)) {
            if (mc.currentScreen != null || !isValidHit) return;

            if (mc.thePlayer.hurtTime == 9) {
                mc.thePlayer.setSprinting(false);
            }
            if (mc.thePlayer.hurtTime == 8 && mc.gameSettings.keyBindForward.isKeyDown()) {
                mc.thePlayer.setSprinting(true);
            }
        } else if (modeSetting.get().equals(Mode.LEGIT_JUMP)) {
            if (mc.currentScreen != null || !isValidHit) return;

            if (mc.thePlayer.hurtTime == 9 && mc.thePlayer.onGround) {
                mc.thePlayer.jump();
            }
        } else if (modeSetting.get().equals(Mode.LEGIT)) {
            if (mc.currentScreen != null) {
                ((IKeyBinding) mc.gameSettings.keyBindForward).setPressed(wPressed);
                return;
            }

            if (!isValidHit) return;

            if (mc.thePlayer.hurtTime == 10) {
                ((IKeyBinding) mc.gameSettings.keyBindForward).setPressed(true);
                mc.thePlayer.setSprinting(true);
            }

            if (mc.thePlayer.hurtTime == 9) {
                mc.thePlayer.setSprinting(false);
            }

            if (mc.thePlayer.hurtTime == 8 && !jumped) {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                    jumped = true;
                }
            }

            if (mc.thePlayer.hurtTime >= 5) {
                mc.thePlayer.setSprinting(true);
                ((IKeyBinding) mc.gameSettings.keyBindForward).setPressed(true);
            }

            if (mc.thePlayer.hurtTime < 5) {
                ((IKeyBinding) mc.gameSettings.keyBindForward).setPressed(wPressed);
                mc.thePlayer.setSprinting(false);
                jumped = false;
            }
        } else if (modeSetting.get().equals(Mode.CUSTOM)) {
            if (!isValidHit) return;

            if (mc.thePlayer.hurtTime == 7) {
                mc.thePlayer.motionX *= 0.6;
                mc.thePlayer.motionZ *= 0.6;
            }
        }
    }

    @Subscribe
    public void oe$PostMotion(EventMotion.Post event) {

    }

    @Subscribe
    public void oe$PacketIn(EventPacket.In event) {
        if (event.packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity velocityPacket = (S12PacketEntityVelocity) event.packet;
            if (velocityPacket.getEntityID() == mc.thePlayer.getEntityId()) {
                if (velocityPacket.getMotionY() > 2140 && velocityPacket.getMotionY() < 3000) {
                    velocityTimer = 0;
                    isValidHit = true;
                    jumped = false;
                } else {
                    isValidHit = false;
                }

                if (modeSetting.get().equals(Mode.CANCEL)) {
                    event.setCanceled(true);
                } else if (modeSetting.get().equals(Mode.PERCENT)) {
                    double hPct = horizontalPercent.get() / 100.0;
                    double vPct = verticalPercent.get() / 100.0;

                    if (hPct == 0.0 && vPct == 0.0) {
                        event.setCanceled(true);
                        return;
                    }

                    velocityPacket.motionX = (int) (velocityPacket.motionX * hPct);
                    velocityPacket.motionY = (int) (velocityPacket.motionY * vPct);
                    velocityPacket.motionZ = (int) (velocityPacket.motionZ * hPct);
                }
            }
        }
    }

    @Subscribe
    public void oe$PacketOut(EventPacket.Out event) {

    }

    private enum Mode implements ModeEnum {
        PERCENT("Percent"),
        CANCEL("Cancel"),
        CUSTOM("Custom"),
        LEGIT("Legit"),
        LEGIT_W("Legit W"),
        LEGIT_JUMP("Legit Jump");

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String displayName() {
            return name;
        }
    }
}