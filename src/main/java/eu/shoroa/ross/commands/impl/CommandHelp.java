package eu.shoroa.ross.commands.impl;

import eu.shoroa.ross.commands.Command;
import eu.shoroa.ross.commands.CommandManager;

public class CommandHelp extends Command {
    public CommandHelp() {
        super(
                "Help",
                "Shows available commands and their usage",
                new String[]{".help", ".help <command>"},
                new String[]{"help", "h"}
        );
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            header("Commands");
            for (Command cmd : CommandManager.commands()) {
                entry(cmd.name, cmd.description);
            }
        } else if (args.length == 1) {
            Command found = null;
            for (Command cmd : CommandManager.commands()) {
                for (String alias : cmd.aliases) {
                    if (alias.equalsIgnoreCase(args[0])) {
                        found = cmd;
                        break;
                    }
                }
            }
            if (found == null) {
                error("Unknown command: " + args[0]);
            } else {
                header(found.name);
                info(found.description);
                for (String line : found.usage) {
                    entry(line);
                }
            }
        } else {
            invalidUsage();
        }
    }
}