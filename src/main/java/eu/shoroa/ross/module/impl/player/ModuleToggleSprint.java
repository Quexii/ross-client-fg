package eu.shoroa.ross.module.impl.player;

import eu.shoroa.ross.mixins.injection.client.settings.KeyBindingAccessor;
import eu.shoroa.ross.module.Bind;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static eu.shoroa.ross.Client.mc;

public class ModuleToggleSprint extends Module {
    public ModuleToggleSprint() {
        super("ToggleSprint", "", Category.PLAYER, null);
    }

    @SubscribeEvent
    public void oe$OnUpdate(LivingEvent.LivingUpdateEvent event) {
        if (mc.playerController == null || mc.thePlayer == null || event.entity != mc.thePlayer) return;

        ((KeyBindingAccessor) mc.gameSettings.keyBindSprint).setPressed(true);
    }
}
