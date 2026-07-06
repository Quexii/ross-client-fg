package eu.shoroa.ross.feature.module.impl.render;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.feature.gui.clickgui.stella.StellaCG;
import eu.shoroa.ross.feature.module.Bind;
import eu.shoroa.ross.feature.module.Category;
import eu.shoroa.ross.feature.module.Module;
import eu.shoroa.ross.render.skia.font.MaterialIcons;
import org.lwjgl.input.Keyboard;

import static eu.shoroa.ross.Client.mc;

public class ModuleClickGUI extends Module {
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
}
