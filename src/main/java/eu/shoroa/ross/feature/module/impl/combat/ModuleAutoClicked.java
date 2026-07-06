package eu.shoroa.ross.feature.module.impl.combat;

import eu.shoroa.ross.event.EventTick;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.mixins.interfaces.IMinecraft;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import eu.shoroa.ross.utils.Timer;
import net.minecraft.client.settings.KeyBinding;
import org.jetbrains.annotations.ApiStatus;

import java.util.Random;

import static eu.shoroa.ross.Client.mc;

public class ModuleAutoClicked extends Module {
    private final SettingCategory categorySettings = addCategory("Settings", ".", "settings");
    private final NumberSetting cps = register(new NumberSetting("CPS", "cps", 10, 1, 20, 1), categorySettings);
    private final NumberSetting randomization = register(new NumberSetting("Randomization", "random", 1.5f, 0, 5, 0.5f), categorySettings);

    private final Random random = new Random();
    private final Timer leftClickTimer = new Timer();
    private long nextLeftDelay;

    public ModuleAutoClicked() {
        super("Auto Clicker", "Automatically clicks for you.", Category.COMBAT, "\uf718");
    }

    @Subscribe
    @ApiStatus.Internal
    public void onEvent(EventTick event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) return;

        handleLeftClick();
    }

    private void handleLeftClick() {
        if (!mc.gameSettings.keyBindAttack.isKeyDown()) {
            nextLeftDelay = 0;
            return;
        }

        ((IMinecraft) mc).setLeftClickCounter(0);

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