package eu.shoroa.ross.module.impl.player;

import eu.shoroa.ross.event.EventLiving;
import eu.shoroa.ross.event.Subscribe;
import eu.shoroa.ross.mixins.injection.client.settings.KeyBindingAccessor;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;

import static eu.shoroa.ross.Client.mc;

public class ModuleToggleSprint extends Module {
    public ModuleToggleSprint() {
        super("ToggleSprint", "", Category.PLAYER, null);
    }

    @Subscribe
    public void oe$OnUpdate(EventLiving.Update event) {
        if (mc.playerController == null || mc.thePlayer == null || event.entity != mc.thePlayer) return;

        ((KeyBindingAccessor) mc.gameSettings.keyBindSprint).setPressed(true);
    }
}
