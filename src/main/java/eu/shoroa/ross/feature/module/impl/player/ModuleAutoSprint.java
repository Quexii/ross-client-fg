package eu.shoroa.ross.feature.module.impl.player;

import eu.shoroa.ross.event.EventLiving;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.mixins.injection.minecraft.client.setting.MixinKeyBinding;
import eu.shoroa.ross.mixins.interfaces.IKeyBinding;
import org.jetbrains.annotations.ApiStatus;

import static eu.shoroa.ross.Client.mc;

public class ModuleAutoSprint extends Module {
    public ModuleAutoSprint() {
        super("Auto Sprint", "Automatically sprints when moving", Category.PLAYER, "\ue566");
    }

    @Subscribe
    @ApiStatus.Internal
    public void onUpdate(EventLiving.Update event) {
        if (mc.playerController == null || mc.thePlayer == null || event.entity != mc.thePlayer) return;

        ((IKeyBinding) mc.gameSettings.keyBindSprint).setPressed(true);
    }
}
