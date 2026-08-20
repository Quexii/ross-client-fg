package eu.shoroa.ross.feature.module;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.module.impl.combat.ModuleAutoClicked;
import eu.shoroa.ross.feature.module.impl.combat.ModuleVelocity;
import eu.shoroa.ross.feature.module.impl.combat.ModuleWTap;
import eu.shoroa.ross.feature.module.impl.hud.*;
import eu.shoroa.ross.feature.module.impl.misc.ModuleBedDefense;
import eu.shoroa.ross.feature.module.impl.misc.ModuleFireballWarning;
import eu.shoroa.ross.feature.module.impl.misc.ModuleSilence;
import eu.shoroa.ross.feature.module.impl.player.*;
import eu.shoroa.ross.feature.module.impl.render.*;
import eu.shoroa.ross.feature.module.impl.render.wheelgui.ModuleWheelGUI;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class ModuleManager {
    private static final List<Module> modulesList = new ArrayList<>();
    private static final List<Module> sortedModulesList = new ArrayList<>();
    private static final Map<Category, Module[]> categoryModules = new HashMap<>();

    public static ModuleFreeLook freeLook;
    public static ModuleFreeCam freecam;
    public static ModuleAntiInvis antiInvis;
    public static ModuleAnimations animations;
    public static ModuleFakeBlock fakeBlock;

    private ModuleManager() {
    }

    public static void init() {
        //combat
        modulesList.add(new ModuleAutoClicked());
        modulesList.add(new ModuleVelocity());
        modulesList.add(new ModuleWTap());
        // render
        modulesList.add(new ModuleClickGUI());
        modulesList.add(new ModuleWheelGUI());
        modulesList.add(new ModuleESP());
        modulesList.add(antiInvis = new ModuleAntiInvis());
        modulesList.add(animations = new ModuleAnimations());
        modulesList.add(fakeBlock = new ModuleFakeBlock());
        modulesList.add(new ModuleTrajectories());
        modulesList.add(new ModuleBlockOverlay());
        // player
        modulesList.add(new ModuleAutoSprint());
        modulesList.add(new ModuleBridgeAssist());
        modulesList.add(freeLook = new ModuleFreeLook());
        modulesList.add(freecam = new ModuleFreeCam());
        modulesList.add(new ModuleFastPlace());
        modulesList.add(new ModuleAutoBridge());
        // hud
        modulesList.add(new ModuleKeystrokes());
        modulesList.add(new ModuleWatermark());
        modulesList.add(new ModuleFPS());
        modulesList.add(new ModulePing());
        modulesList.add(new ModuleClock());
        modulesList.add(new ModuleCoordinates());
        modulesList.add(new ModulePotionEffects());
        modulesList.add(new ModuleArmorHUD());
        modulesList.add(new ModuleArrayList());
        modulesList.add(new ModuleLowHealthOverlay());
        modulesList.add(new ModuleTargetHUD());
        modulesList.add(new ModuleHUDSplash());
        // misc
        modulesList.add(new ModuleFireballWarning());
        modulesList.add(new ModuleBedDefense());
        modulesList.add(new ModuleSilence());

        sortedModulesList.addAll(modulesList);
        Collections.sort(sortedModulesList, Comparator.comparing(m -> m.name));

        for (Category category : Category.values()) {
            List<Module> categoryList = modulesList.stream()
                    .filter(module -> module.category == category)
                    .collect(Collectors.toList());
            categoryModules.put(category, categoryList.toArray(new Module[0]));
        }
    }

    public static void onInput(EventInput event) {
        for (Module module : modulesList) {
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

    public static List<Module> getModules() {
        return modulesList;
    }

    public static List<Module> getSortedModules() {
        return sortedModulesList;
    }

    public static List<Module> getEnabledModules() {
        return modulesList.stream().filter(Module::isEnabled).collect(Collectors.toList());
    }

    public static List<HUDModule> getEnabledHUDModules() {
        return modulesList.stream()
                .filter(Module::isEnabled)
                .filter(module -> module instanceof HUDModule)
                .map(module -> (HUDModule) module)
                .collect(Collectors.toList());
    }

    public static Module[] getModulesByCategory(Category category) {
        return categoryModules.get(category);
    }

    @Nullable
    public static Module getModule(String name) {
        for (Module module : modulesList) {
            if (module.name.replaceAll("[\\s-]", "_").equalsIgnoreCase(name.replaceAll("[\\s-]", "_"))) {
                return module;
            }
        }

        return null;
    }
}
