package eu.shoroa.ross.module;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.module.impl.player.ModuleToggleSprint;
import eu.shoroa.ross.module.impl.render.ModuleClickGui;
import net.minecraftforge.common.MinecraftForge;

public class ModuleManager {
    private static final Module[] modules;

    static {
        modules = new Module[]{
                // combat
                // player
                new ModuleToggleSprint(),
                // render
                new ModuleClickGui()
                // hud
                // misc
        };
    }

    public static void init() {

    }

    public static void onInput(EventInput event) {
        for (Module module : modules) {
            if (module.bind != null && module.bind.key == event.value && module.bind.type == event.type) {
                if (module.bind.action == EventInput.Action.HOLD) {
                    boolean isPressed = event.action == EventInput.Action.PRESS;
                    if (module.isEnabled() != isPressed) {
                        module.setEnabled(isPressed);
                    }
                } else if (module.bind.action == event.action) {
                    module.toggle();
                }
            }
        }
    }
}
