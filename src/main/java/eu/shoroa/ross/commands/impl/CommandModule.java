package eu.shoroa.ross.commands.impl;

import eu.shoroa.ross.Client;
import eu.shoroa.ross.commands.Command;
import eu.shoroa.ross.module.Module;
import eu.shoroa.ross.module.ModuleManager;
import eu.shoroa.ross.settings.Setting;
import eu.shoroa.ross.util.ChatUtil;

public class CommandModule extends Command {
    public CommandModule() {
        super(
                "Module",
                "Toggle modules and change their settings",
                new String[]{
                        ".module toggle <name>",
                        ".module list",
                        ".module set <module> <setting> <value>",
                        ".module settings <name>",
                },
                new String[]{"module", "mod", "m"}
        );
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            invalidUsage();
            return;
        }

        switch (args[0].toLowerCase()) {

            case "list": {
                if (args.length != 1) { invalidUsage(); return; }
                header("Modules");
                for (Module module : ModuleManager.getModules()) {
                    entry(module.name.toLowerCase().replaceAll(" ", ""));
                }
                break;
            }

            case "toggle": {
                if (args.length != 2) { invalidUsage(); return; }
                Module module = ModuleManager.getModule(args[1]);
                if (module == null) {
                    error("Module not found: " + args[1]);
                } else {
                    module.toggle();
                    ChatUtil.toggled(module.name, module.isEnabled());
                    Client.INSTANCE.config.saveQueued();
                }
                break;
            }

            case "settings": {
                if (args.length != 2) { invalidUsage(); return; }
                Module module = ModuleManager.getModule(args[1]);
                if (module == null) {
                    error("Module not found: " + args[1]);
                } else if (module.getSettings().isEmpty()) {
                    info(module.name + " has no settings.");
                } else {
                    header(module.name + " settings");
                    for (Setting setting : module.getSettings()) {
                        entry(setting.getName(), setting.get().toString());
                    }
                }
                break;
            }

            case "set": {
                if (args.length != 4) { invalidUsage(); return; }
                Module module = ModuleManager.getModule(args[1]);
                if (module == null) {
                    error("Module not found: " + args[1]);
                    return;
                }
                Setting setting = module.getSettingById(args[2]);
                if (setting == null) {
                    error("Setting not found: " + args[2]);
                    return;
                }
                if (setting.setFromString(args[3])) {
                    ChatUtil.settingSet(module.name, setting.getName(), setting.get().toString());
                    Client.INSTANCE.config.saveQueued();
                } else {
                    error("Invalid value: " + args[3]);
                }
                break;
            }

            default:
                invalidUsage();
                break;
        }
    }
}