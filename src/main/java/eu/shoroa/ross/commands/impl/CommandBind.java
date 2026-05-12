package eu.shoroa.ross.commands.impl;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.commands.Command;
import eu.shoroa.ross.event.EventInput;
import eu.shoroa.ross.module.Bind;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.module.ModuleManager;
import org.lwjgl.input.Keyboard;

public class CommandBind extends Command {
    public CommandBind() {
        super("Bind", "Sets a bind for a module",
                new String[]{
                        ".bind <module> <value> <action> (value: KEY_#, MOUSE_#) (action: PRESS, RELEASE, HOLD)"
                },
                new String[]{
                        "bind"
                }
        );
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 3) {
            invalidUsage();
            return;
        }

        Module module = ModuleManager.getModule(args[0]);
        if (module == null) {
            error("Module not found: " + args[0]);
            return;
        }

        String action = args[2].toUpperCase();
        EventInput.Action eventAction;
        switch (action) {
            case "PRESS":
                eventAction = EventInput.Action.PRESS;
                break;
            case "RELEASE":
                eventAction = EventInput.Action.RELEASE;
                break;
            case "HOLD":
                eventAction = EventInput.Action.HOLD;
                break;
            default:
                error("Invalid action: " + action + " (must be PRESS, RELEASE or HOLD)");
                return;
        }

        String bind = args[1].toUpperCase();

        if (bind.startsWith("KEY_")) {
            int key = Keyboard.getKeyIndex(bind.substring(4));
            if (key == Keyboard.KEY_NONE) {
                error("Invalid key: " + bind);
                return;
            }
            module.bind = new Bind(key, EventInput.Type.KEYBOARD, eventAction);
            success("Bound " + module.name + " to " + Keyboard.getKeyName(key));
            Client.INSTANCE.config.saveQueued();
        } else if (bind.startsWith("MOUSE_")) {
            try {
                int button = Integer.parseInt(bind.substring(6));
                if (button < 0 || button > 14) {
                    error("Invalid mouse button: " + bind + " (must be MOUSE_0 through MOUSE_14)");
                    return;
                }
                module.bind = new Bind(button, EventInput.Type.MOUSE, eventAction);
                success("Bound " + module.name + " to MOUSE_" + button);
                Client.INSTANCE.config.saveQueued();
            } catch (NumberFormatException e) {
                error("Invalid mouse button: " + bind + " (expected format: MOUSE_0, MOUSE_1 ...)");
            }

        } else {
            error("Invalid bind: " + bind + " (must start with KEY_ or MOUSE_)");
        }
    }
}