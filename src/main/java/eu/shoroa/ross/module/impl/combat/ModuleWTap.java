package eu.shoroa.ross.module.impl.combat;

import eu.shoroa.ross.event.EventLiving;
import eu.shoroa.ross.event.EventSelfUpdate;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.settings.NumberSetting;
import eu.shoroa.ross.util.ChatUtil;
import eu.shoroa.ross.util.Timer;
import net.minecraft.network.play.client.C0BPacketEntityAction;

import static eu.shoroa.ross.Client.mc;

public class ModuleWTap extends Module {
    private final NumberSetting preDelay = register(new NumberSetting("Pre-Delay", "pre_delay", 0, 0, 500, 10));
    private final NumberSetting holdTime = register(new NumberSetting("Hold Time", "hold_time", 100, 0, 500, 10));
    private final NumberSetting randomization = register(new NumberSetting("Randomization", "randomization", 0, 0, 100, 10));

    private final Timer actionTimer = new Timer();

    private boolean isTapping = false;
    private boolean isHolding = false;

    private long calculatedPreDelay;
    private long calculatedHoldTime;

    public ModuleWTap() {
        super("W-Tap", "Automatically releases and presses W when attacking to reset sprint.", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        isTapping = false;
        isHolding = false;
        super.onDisable();
    }

    @Subscribe
    public void oe$LivingDamage(EventLiving.Attack event) {
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
    public void oe$Update(EventSelfUpdate event) {
        if (!isTapping) return;

        if (!isHolding) {
            if (actionTimer.elapsed(calculatedPreDelay, true)) {

                mc.thePlayer.setSprinting(false);

                isHolding = true;
            }
        }
        else {
            if (actionTimer.elapsed(calculatedHoldTime, true)) {

                mc.thePlayer.setSprinting(true);

                isTapping = false;
                isHolding = false;
            }
        }
    }
}