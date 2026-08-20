package eu.shoroa.ross.feature.module.impl.render;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaCG;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaTheme;
import eu.shoroa.ross.feature.module.Bind;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.feature.setting.ModeEnum;
import eu.shoroa.ross.feature.setting.ModeSetting;
import eu.shoroa.ross.feature.setting.SettingCategory;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import org.lwjgl.input.Keyboard;

import static eu.shoroa.ross.Client.mc;

public class ModuleClickGUI extends Module {
    private final SettingCategory settings = addCategory("Settings", "settings", "settings");
    private final ModeSetting themeMode = (ModeSetting) register(
            new ModeSetting<>("Theme", "theme", Theme.LIGHT)
                    .onChange(newTheme -> StellaTheme.set(newTheme.theme)),
            settings);

    public ModuleClickGUI() {
        super("Click GUI", ".", Category.RENDER, new Bind(Keyboard.KEY_RSHIFT, EventInput.Type.KEYBOARD, EventInput.Action.PRESS), MaterialIcons.TUNE);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        toggle();
        if (!(mc.currentScreen instanceof StellaCG)) {
            mc.displayGuiScreen(StellaCG.getInstance());
        }
    }

    private enum Theme implements ModeEnum {
        LIGHT("Light", StellaTheme.LIGHT),
        DARK("Dark", StellaTheme.DARK),
        FOREST("Forest", StellaTheme.FOREST),
        SLATE("Slate", StellaTheme.SLATE),
        EMBER("Ember", StellaTheme.EMBER),
        ROSE("Rose", StellaTheme.ROSE),
        AURORA("Aurora", StellaTheme.AURORA),
        ABYSS("Abyss", StellaTheme.ABYSS),
        ;

        private final String name;
        private final StellaTheme theme;

        Theme(String name, StellaTheme theme) {
            this.name = name;
            this.theme = theme;
        }

        @Override
        public String displayName() {
            return name;
        }
    }
}
