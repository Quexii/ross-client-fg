package eu.shoroa.ross.feature.command.impl;

import eu.shoroa.ross.event.EventSelfUpdate;
import eu.shoroa.ross.event.api.Subscribe;
import eu.shoroa.ross.feature.command.Command;
import eu.shoroa.ross.feature.gui.editor.HUDEditor;
import org.jetbrains.annotations.ApiStatus;

import static eu.shoroa.ross.Client.EVENT_BUS;
import static eu.shoroa.ross.Client.mc;

public class CommandEditor extends Command {
    public CommandEditor() {
        super("Editor", "Opens the HUD editor", new String[]{"editor"}, new String[]{"editor", "hud"});
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            EVENT_BUS.register(this);
        } else {
            invalidUsage();
        }
    }

    @Subscribe
    @ApiStatus.Internal
    public void onUpdate(EventSelfUpdate event) {
        if (mc.currentScreen == null) {
            mc.displayGuiScreen(new HUDEditor());

            EVENT_BUS.unregister(this);
        }
    }
}
