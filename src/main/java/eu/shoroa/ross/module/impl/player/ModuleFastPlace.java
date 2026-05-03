package eu.shoroa.ross.module.impl.player;

import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.mixins.injection.client.MinecraftAccessor;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.settings.NumberSetting;
import eu.shoroa.ross.util.Timer;

import static eu.shoroa.ross.Client.mc;

public class ModuleFastPlace extends Module {
    private final NumberSetting cps = register(new NumberSetting("CPS", "fastplace.cps", 10, 1, 20, 1));
    private final NumberSetting activationDelay = register(new NumberSetting("Activation Delay", "fastplace.activation_delay", 300, 0, 2000, 50));

    private final Timer timer = new Timer();
    private final Timer holdTimer = new Timer();
    private boolean wasKeyDown = false;

    public ModuleFastPlace() {
        super("FastPlace", "Place blocks faster", Category.PLAYER, null);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        timer.reset();
        holdTimer.reset();
        wasKeyDown = false;
    }

    @Subscribe
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
            ((MinecraftAccessor) mc).setRightClickDelayTimer(0);
        }
    }
}
