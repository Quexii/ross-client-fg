package eu.shoroa.ross.commands;

import eu.shoroa.ross.commands.impl.CommandHelp;

public class CommandManager {
    private static final Command[] commands;

    static {
        commands = new Command[]{
                new CommandHelp()
        };
    }

    public static void handleCommands(String message) {
        String command = message.split(" ")[0];
        String[] args = message.split(" ");

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
