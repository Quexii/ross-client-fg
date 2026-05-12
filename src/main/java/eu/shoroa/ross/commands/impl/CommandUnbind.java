package eu.shoroa.ross.commands.impl;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.commands.Command;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.module.ModuleManager;

public class CommandUnbind extends Command {
    public CommandUnbind() {
        super("Unbind", "Removes the bind from a module",
                new String[]{".unbind <module>"},
                new String[]{"unbind", "ub"}
        );
    }

    @Override
    public void execute(String[] args) {
        if (args.length != 1) {
            invalidUsage();
            return;
        }

        Module module = ModuleManager.getModule(args[0]);
        if (module == null) {
            error("Module not found: " + args[0]);
            return;
        }

        if (module.bind == null) {
            info(module.name + " has no bind.");
            return;
        }

        module.bind = null;
        success("Unbound " + module.name + ".");
        Client.INSTANCE.config.saveQueued();
    }
}