package eu.shoroa.ross.module.impl.render;

import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.gui.clickgui.ScreenClickGUI;
import eu.shoroa.ross.module.Bind;
import eu.shoroa.ross.module.Category;
import eu.shoroa.ross.module.Module;
import org.lwjgl.input.Keyboard;

import static eu.shoroa.ross.Client.mc;

public class ModuleClickGui extends Module {
    private final ScreenClickGUI screen = new ScreenClickGUI();

    public ModuleClickGui() {
        super("ClickGUI", "", Category.RENDER, new Bind(Keyboard.KEY_RSHIFT, EventInput.Type.KEYBOARD, EventInput.Action.PRESS));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        mc.displayGuiScreen(screen);
        toggle();
    }
}
