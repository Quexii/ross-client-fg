package eu.shoroa.ross.module;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.module.impl.combat.*;
import eu.shoroa.ross.module.impl.hud.*;
import eu.shoroa.ross.module.impl.misc.ModuleBedDefenseDisplay;
import eu.shoroa.ross.module.impl.misc.ModuleBedwars;
import eu.shoroa.ross.module.impl.misc.ModuleFireballWarning;
import eu.shoroa.ross.module.impl.misc.ModuleFreeLook;
import eu.shoroa.ross.module.impl.player.*;
import eu.shoroa.ross.module.impl.render.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ModuleManager {
    private static final Module[] modules;
    private static final List<Module> modulesList = new ArrayList<>();
    private static final List<Module> modulesListSorted = new ArrayList<>();
    ;
    private static final Map<Category, Module[]> categoryModules = new HashMap<>();

    public static final ModuleAntiInvis antiInvis;
    public static final ModuleFreeLook freeLook;
    public static final ModuleFreecam freecam;
    public static final ModuleAnimations animations;
    public static final ModuleFakeBlock fakeBlock;
    public static final ModuleAntiBot antiBot;

    static {
        modules = new Module[]{
                // combat
                new ModuleAutoClicked(),
                antiBot = new ModuleAntiBot(),
                new ModuleAimBot(),
                new ModuleVelocity(),
                new ModuleWTap(),
                // player
                new ModuleToggleSprint(),
                new ModuleBridgeAssist(),
                new ModuleFastPlace(),
                freecam = new ModuleFreecam(),
                // render
                new ModuleClickGui(),
                new ModuleTrajectories(),
                new ModuleBlockOverlay(),
                antiInvis = new ModuleAntiInvis(),
                new ModuleESP(),
                animations = new ModuleAnimations(),
                fakeBlock = new ModuleFakeBlock(),
                // hud
                new ModuleArrayList(),
                new ModuleWatermark(),
                new ModuleTargetHUD(),
                new ModuleItemNotifs(),
                new ModuleModernHUD(),
                new ModuleLowHealthOverlay(),
                // misc
                freeLook = new ModuleFreeLook(),
                new ModuleFireballWarning(),
//                new ModuleBedwars(),
                new ModuleBedDefenseDisplay()
        };

        modulesList.addAll(Arrays.asList(modules));
        modulesListSorted.addAll(modulesList);
        modulesListSorted.sort(Comparator.comparing(m -> m.name));

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

    public static List<Module> getSortedModules() {
        return modulesListSorted;
    }

    public static List<Module> getEnabledModules() {
        return modulesList.stream().filter(Module::isEnabled).collect(Collectors.toList());
    }

    public static Module[] getModulesByCategory(Category category) {
        return categoryModules.get(category);
    }

    @Nullable
    public static Module getModule(String name) {
        for (Module module : modules) {
            if (module.name.replaceAll(" ","").equalsIgnoreCase(name.replaceAll(" ",""))) {
                return module;
            }
        }

        return null;
    }
}
