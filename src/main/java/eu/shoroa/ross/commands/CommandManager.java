package eu.shoroa.ross.commands;

import eu.shoroa.ross.commands.impl.*;

import java.util.Arrays;

public class CommandManager {
    private static final Command[] commands;

    static {
        commands = new Command[]{
                new CommandHelp(),
                new CommandModule(),
                new CommandSay(),
                new CommandBind(),
                new CommandUnbind()
        };
    }

    public static void handleCommands(String message) {
        String[] parts = message.split(" ");
        String command = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        for (Command cmd : commands) {
            for (String alias : cmd.aliases) {
                if (alias.equalsIgnoreCase(command)) {
                    cmd.execute(args);
                }
            }
        }
    }

    public static Command[] commands() {
        return commands;
    }
}
