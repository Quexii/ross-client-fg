package eu.shoroa.ross.commands.impl;

import eu.shoroa.ross.commands.Command;
import eu.shoroa.ross.commands.CommandManager;
import net.minecraft.util.EnumChatFormatting;

public class CommandHelp extends Command {
    public CommandHelp() {
        super("Help", "Shows you the list of commands and info about them", new String[]{".help", ".help <command>"}, new String[]{"help", "h"});
    }

    @Override
    public void execute(String[] args) {
        if (args.length == 0) {
            Command[] commands = CommandManager.commands();
            clientMessage(EnumChatFormatting.GRAY + "Commands:");
            for (Command command : commands) {
                clientMessage(">" + command.name + " - " + command.description);
            }
        } else if (args.length == 1) {
            clientMessage(">" + args[0] + " -");
            clientMessage(EnumChatFormatting.GRAY + "Usage: " + EnumChatFormatting.WHITE + usage[0]);
        } else {
            clientMessage(EnumChatFormatting.RED + "Invalid arguments, usage:");
            for (String s : usage) {
                clientMessage(EnumChatFormatting.RED + "  -  " + s);
            }
        }
    }
}
