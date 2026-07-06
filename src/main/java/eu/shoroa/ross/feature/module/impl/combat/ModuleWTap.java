package eu.shoroa.ross.feature.module.impl.combat;

import eu.shoroa.ross.event.EventLiving;
import eu.shoroa.ross.event.EventSelfUpdate;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.NumberSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.utils.Timer;

import static eu.shoroa.ross.Client.mc;

public class ModuleWTap extends Module {
    private final SettingCategory categorySettings = addCategory("Settings", ".", "settings");
    private final NumberSetting preDelay = register(new NumberSetting("Pre-Delay", "pre_delay", 0, 0, 500, 10), categorySettings);
    private final NumberSetting holdTime = register(new NumberSetting("Hold Time", "hold_time", 100, 0, 500, 10), categorySettings);
    private final NumberSetting randomization = register(new NumberSetting("Randomization", "randomization", 0, 0, 100, 10), categorySettings);

    private final Timer actionTimer = new Timer();

    private boolean isTapping = false;
    private boolean isHolding = false;

    private long calculatedPreDelay;
    private long calculatedHoldTime;

    public ModuleWTap() {
        super("W-Tap", "Automatically releases and presses W when attacking to reset sprint.", Category.COMBAT, "\uf4b5");
    }

    @Override
    public void onDisable() {
        isTapping = false;
        isHolding = false;
        super.onDisable();
    }

    @Subscribe
    public void onLivingDamage(EventLiving.Attack event) {
        if (event.entity.equals(mc.thePlayer)) {
            long rand = (long) (Math.random() * randomization.get());

            calculatedPreDelay = preDelay.get().longValue() + rand;
            calculatedHoldTime = holdTime.get().longValue() + rand;

            isTapping = true;
            isHolding = false;
            actionTimer.reset();
        }
    }

    @Subscribe
    public void onUpdate(EventSelfUpdate event) {
        if (!isTapping) return;

        if (!isHolding) {
            if (actionTimer.elapsed(calculatedPreDelay, true)) {

                mc.thePlayer.setSprinting(false);

                isHolding = true;
            }
        } else {
            if (actionTimer.elapsed(calculatedHoldTime, true)) {

                mc.thePlayer.setSprinting(true);

                isTapping = false;
                isHolding = false;
            }
        }
    }
}