package eu.shoroa.ross.module;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.module.impl.combat.ModuleAutoClicked;
import eu.shoroa.ross.module.impl.hud.ModuleItemNotifs;
import eu.shoroa.ross.module.impl.hud.ModuleModernHUD;
import eu.shoroa.ross.module.impl.hud.ModuleTargetHUD;
import eu.shoroa.ross.module.impl.hud.ModuleWatermark;
import eu.shoroa.ross.module.impl.misc.ModuleBedwars;
import eu.shoroa.ross.module.impl.misc.ModuleFireballWarning;
import eu.shoroa.ross.module.impl.misc.ModuleFreeLook;
import eu.shoroa.ross.module.impl.player.*;
import eu.shoroa.ross.module.impl.render.*;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ModuleManager {
    private static final Module[] modules;
    private static final Map<Category, Module[]> categoryModules = new HashMap<>();

    public static final ModuleAntiInvis antiInvis;
    public static final ModuleFreeLook freeLook;
    public static final ModuleAnimations animations;
    public static final ModuleFakeBlock fakeBlock;

    static {
        modules = new Module[]{
                // combat
                new ModuleAutoClicked(),
                // player
                new ModuleToggleSprint(),
                new ModuleBridgeAssist(),
                new ModuleFastPlace(),
                // render
                new ModuleClickGui(),
                new ModuleTrajectories(),
                new ModuleBlockOverlay(),
                antiInvis = new ModuleAntiInvis(),
                new ModuleESP(),
                animations = new ModuleAnimations(),
                fakeBlock = new ModuleFakeBlock(),
                // hud
                new ModuleWatermark(),
                new ModuleTargetHUD(),
                new ModuleItemNotifs(),
                new ModuleModernHUD(),
                // misc
                freeLook = new ModuleFreeLook(),
                new ModuleFireballWarning(),
                new ModuleBedwars()
        };

        for (Category category : Category.values()) {
            categoryModules.put(category, Arrays.stream(modules).filter(m -> m.category == category).toArray(Module[]::new));
        }
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

    public static Module[] getModules() {
        return modules;
    }

    public static Module[] getModulesByCategory(Category category) {
        return categoryModules.get(category);
    }

    @Nullable
    public static Module getModule(String name) {
        for (Module module : modules) {
            if (module.name.equalsIgnoreCase(name)) {
                return module;
            }
        }

        return null;
    }
}
