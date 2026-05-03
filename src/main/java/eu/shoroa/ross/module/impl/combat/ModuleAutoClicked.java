package eu.shoroa.ross.module.impl.combat;

import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.mixins.injection.client.MinecraftAccessor;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.settings.NumberSetting;
import eu.shoroa.ross.util.Timer;
import net.minecraft.client.settings.KeyBinding;

import java.util.Random;

import static eu.shoroa.ross.Client.mc;

public class ModuleAutoClicked extends Module {

    private final NumberSetting cps = register(new NumberSetting("CPS", "autoclicker.cps", 10, 1, 20, 1));
    private final NumberSetting randomization = register(new NumberSetting("Randomization", "autoclicker.random", 1.5f, 0, 5, 0.5f));

    private final Random random = new Random();
    private final Timer leftClickTimer = new Timer();
    private long nextLeftDelay;

    public ModuleAutoClicked() {
        super("AutoClicker", "Automatically clicks for you", Category.COMBAT, null);
    }

    @Subscribe
    public void onEvent(EventTick event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) return;

        handleLeftClick();
    }

    private void handleLeftClick() {
        if (!mc.gameSettings.keyBindAttack.isKeyDown()) {
            nextLeftDelay = 0;
            return;
        }

        ((MinecraftAccessor) mc).setLeftClickCounter(0);

        if (nextLeftDelay == 0)
            calculateNextLeftDelay();

        if (leftClickTimer.elapsed(nextLeftDelay, true)) {

            KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());

            calculateNextLeftDelay();
        }
    }

    private void calculateNextLeftDelay() {
        long baseDelay = (long) (1000 / cps.get());
        long randomOffset = (long) ((random.nextFloat() - 0.5f) * 2 * randomization.get() * 20);
        nextLeftDelay = Math.max(30, baseDelay + randomOffset);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        leftClickTimer.reset();
        nextLeftDelay = 0;
    }
}
