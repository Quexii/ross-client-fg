package eu.shoroa.ross.feature.module.impl.player;

import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.mixins.interfaces.IMinecraft;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.utils.Timer;
import org.jetbrains.annotations.ApiStatus;

import static eu.shoroa.ross.Client.mc;

public class ModuleFastPlace extends Module {
    private final SettingCategory categorySettings = addCategory("Settings", ".", "settings");
    private final NumberSetting cps = register(new NumberSetting("CPS", "cps", 10, 1, 20, 1), categorySettings);
    private final NumberSetting activationDelay = register(new NumberSetting("Activation Delay", "activation_delay", 300, 0, 2000, 50), categorySettings);

    private final Timer timer = new Timer();
    private final Timer holdTimer = new Timer();
    private boolean wasKeyDown = false;

    public ModuleFastPlace() {
        super("Fast Place", "Place blocks faster.", Category.PLAYER, "\uef9c");
    }

    @Override
    public void onEnable() {
        super.onEnable();
        timer.reset();
        holdTimer.reset();
        wasKeyDown = false;
    }

    @Subscribe
    @ApiStatus.Internal
    public void onTick(EventTick event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) return;

        boolean keyDown = mc.gameSettings.keyBindUseItem.isKeyDown();

        if (!keyDown) {
            wasKeyDown = false;
            return;
        }

        if (!wasKeyDown) {
            holdTimer.reset();
            wasKeyDown = true;
        }

        if (!holdTimer.elapsed(activationDelay.get().longValue(), false)) return;

        long delay = (long) (1000f / cps.get());
        if (timer.elapsed(delay, true)) {
            ((IMinecraft) mc).setRightClickDelayTimer(0);
        }
    }
}