package eu.shoroa.ross.feature.command;

import eu.shoroa.ross.feature.command.impl.CommandEditor;

import java.util.Arrays;

public class CommandManager {
    private static final Command[] commands;

    static {
        commands = new Command[]{
                new CommandEditor()
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